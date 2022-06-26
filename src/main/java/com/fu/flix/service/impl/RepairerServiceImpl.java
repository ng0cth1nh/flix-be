package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.dao.*;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.RepairerApproveRequest;
import com.fu.flix.dto.request.RequestingDetailForRepairerRequest;
import com.fu.flix.dto.response.RepairerApproveResponse;
import com.fu.flix.dto.response.RequestingDetailForRepairerResponse;
import com.fu.flix.entity.*;
import com.fu.flix.service.RepairerService;
import com.fu.flix.util.DateFormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

import static com.fu.flix.constant.Constant.*;
import static com.fu.flix.constant.enums.Status.*;
import static com.fu.flix.constant.enums.TransactionType.PAY_COMMISSIONS;

@Service
@Slf4j
@Transactional
public class RepairerServiceImpl implements RepairerService {
    private final RepairerDAO repairerDAO;
    private final RepairRequestDAO repairRequestDAO;
    private final RepairRequestMatchingDAO repairRequestMatchingDAO;
    private final UserDAO userDAO;
    private final ImageDAO imageDAO;
    private final InvoiceDAO invoiceDAO;
    private final BalanceDAO balanceDAO;
    private final AppConf appConf;
    private final TransactionHistoryDAO transactionHistoryDAO;
    private final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public RepairerServiceImpl(RepairerDAO repairerDAO,
                               RepairRequestDAO repairRequestDAO,
                               RepairRequestMatchingDAO repairRequestMatchingDAO,
                               UserDAO userDAO,
                               ImageDAO imageDAO,
                               InvoiceDAO invoiceDAO,
                               BalanceDAO balanceDAO,
                               AppConf appConf,
                               TransactionHistoryDAO transactionHistoryDAO) {
        this.repairerDAO = repairerDAO;
        this.repairRequestDAO = repairRequestDAO;
        this.repairRequestMatchingDAO = repairRequestMatchingDAO;

        this.userDAO = userDAO;
        this.imageDAO = imageDAO;
        this.invoiceDAO = invoiceDAO;
        this.balanceDAO = balanceDAO;
        this.appConf = appConf;
        this.transactionHistoryDAO = transactionHistoryDAO;
    }

    @Override
    public ResponseEntity<RepairerApproveResponse> approveRequest(RepairerApproveRequest request) {
        String requestCode = getRequestCode(request.getRequestCode());

        Optional<RepairRequest> optionalRepairRequest = repairRequestDAO.findByRequestCode(requestCode);

        if (optionalRepairRequest.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_REQUEST_CODE);
        }

        RepairRequest repairRequest = optionalRepairRequest.get();
        if (!PENDING.getId().equals(repairRequest.getStatusId())) {
            throw new GeneralException(HttpStatus.CONFLICT, JUST_CAN_ACCEPT_PENDING_REQUEST);
        }

        Repairer repairer = repairerDAO.findByUserId(request.getUserId()).get();
        if (repairer.isRepairing()) {
            throw new GeneralException(HttpStatus.CONFLICT, CAN_NOT_ACCEPT_REQUEST_WHEN_ON_ANOTHER_FIXING);
        }

        Invoice invoice = invoiceDAO.findByRequestCode(requestCode).get();
        Balance balance = balanceDAO.findByUserId(repairer.getUserId()).get();
        Double neededBalance = invoice.getActualProceeds() * this.appConf.getProfitRate();
        if (balance.getBalance() < neededBalance) {
            throw new GeneralException(HttpStatus.CONFLICT, BALANCE_MUST_GREATER_THAN_OR_EQUAL_ + neededBalance);
        }

        minusCommissions(balance, neededBalance, invoice.getRequestCode());
        repairer.setRepairing(true);
        repairRequest.setStatusId(APPROVED.getId());

        RepairRequestMatching repairRequestMatching = buildRepairRequestMatching(requestCode, repairer.getUserId());
        repairRequestMatchingDAO.save(repairRequestMatching);

        RepairerApproveResponse response = new RepairerApproveResponse();
        response.setMessage(APPROVAL_REQUEST_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void minusCommissions(Balance balance, Double neededBalance, String requestCode) {
        balance.setBalance(balance.getBalance() - neededBalance);
        TransactionHistory transactionHistory = new TransactionHistory();
        transactionHistory.setBalanceId(balance.getId());
        transactionHistory.setAmount(neededBalance);
        transactionHistory.setType(PAY_COMMISSIONS.name());
        transactionHistory.setRequestCode(requestCode);
        transactionHistoryDAO.save(transactionHistory);
    }

    private RepairRequestMatching buildRepairRequestMatching(String requestCode, Long repairerId) {
        RepairRequestMatching repairRequestMatching = new RepairRequestMatching();
        repairRequestMatching.setRequestCode(requestCode);
        repairRequestMatching.setRepairerId(repairerId);
        return repairRequestMatching;
    }

    @Override
    public ResponseEntity<RequestingDetailForRepairerResponse> getRepairRequestDetail(RequestingDetailForRepairerRequest request) {
        String requestCode = getRequestCode(request.getRequestCode());
        RequestingDetailForRepairerResponse response = new RequestingDetailForRepairerResponse();
        Optional<RepairRequest> optionalRepairRequest = repairRequestDAO.findByRequestCode(requestCode);

        if (optionalRepairRequest.isPresent()) {
            RepairRequest repairRequest = optionalRepairRequest.get();

            if (isNotPending(repairRequest)) {
                User repairer = userDAO.findByUsername(request.getUsername()).get();
                RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();
                if (isNotMatchRepairer(repairer, repairRequestMatching)) {
                    throw new GeneralException(HttpStatus.NOT_ACCEPTABLE, REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_GET_THIS_REQUEST_DETAIL);
                }
            }

            Long customerId = repairRequest.getUserId();
            User customer = userDAO.findById(customerId).get();
            Image avatarImage = imageDAO.findById(customer.getAvatar()).get();

            response.setCustomerName(customer.getFullName());
            response.setAvatar(avatarImage.getUrl());
            response.setCustomerId(customerId);
            response.setServiceId(repairRequest.getServiceId());
            response.setAddressId(repairRequest.getAddressId());
            response.setExpectFixingTime(DateFormatUtil.toString(repairRequest.getExpectStartFixingAt(), DATE_TIME_PATTERN));
            response.setDescription(repairRequest.getDescription());
            response.setVoucherId(repairRequest.getVoucherId());
            response.setPaymentMethodId(repairRequest.getPaymentMethodId());
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private boolean isNotPending(RepairRequest repairRequest) {
        return !PENDING.getId().equals(repairRequest.getStatusId());
    }

    private boolean isNotMatchRepairer(User repairer, RepairRequestMatching repairRequestMatching) {
        return !repairer.getId().equals(repairRequestMatching.getRepairerId());
    }

    private String getRequestCode(String requestCode) {
        return requestCode == null ? Strings.EMPTY : requestCode;
    }
}
