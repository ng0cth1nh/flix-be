package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.enums.PaymentMethod;
import com.fu.flix.constant.enums.RepairerSuggestionType;
import com.fu.flix.constant.enums.RoleType;
import com.fu.flix.constant.enums.RequestStatus;
import com.fu.flix.dao.*;
import com.fu.flix.dto.HistoryRequestForRepairerDTO;
import com.fu.flix.dto.IHistoryRequestForRepairerDTO;
import com.fu.flix.dto.IRequestingDTO;
import com.fu.flix.dto.RequestingDTO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.entity.*;
import com.fu.flix.service.AddressService;
import com.fu.flix.service.CustomerService;
import com.fu.flix.service.RepairerService;
import com.fu.flix.service.ValidatorService;
import com.fu.flix.util.DateFormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fu.flix.constant.Constant.*;
import static com.fu.flix.constant.enums.RepairerSuggestionType.SUGGESTED;
import static com.fu.flix.constant.enums.RequestStatus.*;
import static com.fu.flix.constant.enums.TransactionType.*;

@Service
@Slf4j
@Transactional
public class RepairerServiceImpl implements RepairerService {
    private final RepairerDAO repairerDAO;
    private final RepairRequestDAO repairRequestDAO;
    private final RepairRequestMatchingDAO repairRequestMatchingDAO;
    private final ImageDAO imageDAO;
    private final InvoiceDAO invoiceDAO;
    private final BalanceDAO balanceDAO;
    private final AppConf appConf;
    private final TransactionHistoryDAO transactionHistoryDAO;
    private final CustomerService customerService;
    private final ValidatorService validatorService;
    private final UserAddressDAO userAddressDAO;
    private final AddressService addressService;
    private final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public RepairerServiceImpl(RepairerDAO repairerDAO,
                               RepairRequestDAO repairRequestDAO,
                               RepairRequestMatchingDAO repairRequestMatchingDAO,
                               ImageDAO imageDAO,
                               InvoiceDAO invoiceDAO,
                               BalanceDAO balanceDAO,
                               AppConf appConf,
                               TransactionHistoryDAO transactionHistoryDAO,
                               CustomerService customerService,
                               ValidatorService validatorService,
                               UserAddressDAO userAddressDAO,
                               AddressService addressService) {
        this.repairerDAO = repairerDAO;
        this.repairRequestDAO = repairRequestDAO;
        this.repairRequestMatchingDAO = repairRequestMatchingDAO;

        this.imageDAO = imageDAO;
        this.invoiceDAO = invoiceDAO;
        this.balanceDAO = balanceDAO;
        this.appConf = appConf;
        this.transactionHistoryDAO = transactionHistoryDAO;
        this.customerService = customerService;
        this.validatorService = validatorService;
        this.userAddressDAO = userAddressDAO;
        this.addressService = addressService;
    }

    @Override
    public ResponseEntity<RepairerApproveResponse> approveRequest(RepairerApproveRequest request) {
        String requestCode = getRequestCodeNotNull(request.getRequestCode());

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
        Long neededBalance = (long) (invoice.getActualProceeds() * this.appConf.getProfitRate());
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

    private void minusCommissions(Balance balance, Long neededBalance, String requestCode) {
        balance.setBalance(balance.getBalance() - neededBalance);
        TransactionHistory transactionHistory = new TransactionHistory();
        transactionHistory.setUserId(balance.getUserId());
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
        String requestCode = getRequestCodeNotNull(request.getRequestCode());
        RequestingDetailForRepairerResponse response = new RequestingDetailForRepairerResponse();
        Optional<RepairRequest> optionalRepairRequest = repairRequestDAO.findByRequestCode(requestCode);

        if (optionalRepairRequest.isPresent()) {
            RepairRequest repairRequest = optionalRepairRequest.get();

            if (isNotPending(repairRequest)) {
                User repairer = validatorService.getUserValidated(request.getUsername());
                RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();
                if (isNotMatchRepairer(repairer, repairRequestMatching)) {
                    throw new GeneralException(HttpStatus.NOT_ACCEPTABLE, REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_GET_THIS_REQUEST_DETAIL);
                }
            }

            Long customerId = repairRequest.getUserId();
            User customer = validatorService.getUserValidated(customerId);
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

    @Override
    public ResponseEntity<CancelRequestForRepairerResponse> cancelFixingRequest(CancelRequestForRepairerRequest request) {
        String requestCode = getRequestCodeNotNull(request.getRequestCode());

        RepairRequest repairRequest = customerService.getRepairRequest(requestCode);
        RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();
        if (!repairRequestMatching.getRepairerId().equals(request.getUserId())) {
            throw new GeneralException(HttpStatus.GONE, USER_DOES_NOT_HAVE_PERMISSION_TO_CANCEL_THIS_REQUEST);
        }

        if (!isCancelable(repairRequest)) {
            throw new GeneralException(HttpStatus.GONE, ONLY_CAN_CANCEL_REQUEST_FIXING_OR_APPROVED);
        }

        customerService.updateRepairerAfterCancelRequest(requestCode);
        if (isFined(repairRequest)) {
            monetaryFine(request.getUserId(), requestCode);
        }

        customerService.updateUsedVoucherQuantityAfterCancelRequest(repairRequest);
        updateRepairRequest(request, repairRequest);

        CancelRequestForRepairerResponse response = new CancelRequestForRepairerResponse();
        response.setMessage(CANCEL_REPAIR_REQUEST_SUCCESSFUL);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void monetaryFine(Long repairerId, String requestCode) {
        Long fineMoney = this.appConf.getFine();
        Repairer repairer = repairerDAO.findByUserId(repairerId).get();
        Balance balance = balanceDAO.findByUserId(repairer.getUserId()).get();

        balance.setBalance(balance.getBalance() - fineMoney);

        TransactionHistory finedTransaction = new TransactionHistory();
        finedTransaction.setUserId(repairerId);
        finedTransaction.setAmount(fineMoney);
        finedTransaction.setType(FINED.name());
        finedTransaction.setRequestCode(requestCode);
        transactionHistoryDAO.save(finedTransaction);
    }

    private boolean isCancelable(RepairRequest repairRequest) {
        String statusId = repairRequest.getStatusId();
        return APPROVED.getId().equals(statusId) || FIXING.getId().equals(statusId);
    }

    private void updateRepairRequest(CancelRequestForRepairerRequest request, RepairRequest repairRequest) {
        repairRequest.setStatusId(CANCELLED.getId());
        repairRequest.setCancelledByRoleId(RoleType.ROLE_REPAIRER.getId());
        repairRequest.setReasonCancel(request.getReason());
    }

    private boolean isFined(RepairRequest repairRequest) {
        Duration duration = Duration.between(LocalDateTime.now(), repairRequest.getExpectStartFixingAt());
        return duration.getSeconds() < this.appConf.getMinTimeFined() || FIXING.getId().equals(repairRequest.getStatusId());
    }

    @Override
    public ResponseEntity<HistoryRequestForRepairerResponse> getFixingRequestHistories(HistoryRequestForRepairerRequest request) {
        List<IHistoryRequestForRepairerDTO> historyDTOs = repairRequestMatchingDAO
                .findRequestHistoriesForRepairerByStatus(request.getUserId(), getStatusIdValidated(request.getStatus()));

        List<HistoryRequestForRepairerDTO> requestHistories = historyDTOs.stream()
                .map(h -> {
                    HistoryRequestForRepairerDTO dto = new HistoryRequestForRepairerDTO();
                    dto.setRequestCode(h.getRequestCode());
                    dto.setStatus(h.getStatus());
                    dto.setImage(h.getImage());
                    dto.setServiceName(h.getServiceName());
                    dto.setDescription(h.getDescription());
                    dto.setPrice(h.getPrice());
                    dto.setActualPrice(h.getActualPrice());
                    dto.setDate(DateFormatUtil.toString(h.getCreatedAt(), DATE_TIME_PATTERN));

                    return dto;
                }).collect(Collectors.toList());

        HistoryRequestForRepairerResponse response = new HistoryRequestForRepairerResponse();
        response.setRequestHistories(requestHistories);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String getStatusIdValidated(String status) {
        for (RequestStatus s : RequestStatus.values()) {
            if (s.name().equals(status)) {
                return s.getId();
            }
        }

        throw new GeneralException(HttpStatus.GONE, INVALID_STATUS);
    }

    @Override
    public ResponseEntity<CreateInvoiceResponse> createInvoice(CreateInvoiceRequest request) {
        String requestCode = getRequestCodeNotNull(request.getRequestCode());

        Optional<RepairRequest> optionalRepairRequest = repairRequestDAO.findByRequestCode(requestCode);
        if (optionalRepairRequest.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_REQUEST_CODE);
        }

        RepairRequest repairRequest = optionalRepairRequest.get();
        if (!FIXING.getId().equals(repairRequest.getStatusId())) {
            throw new GeneralException(HttpStatus.GONE, JUST_CAN_CREATE_INVOICE_WHEN_REQUEST_STATUS_IS_FIXING);
        }

        repairRequest.setStatusId(PAYMENT_WAITING.getId());

        Invoice invoice = invoiceDAO.findByRequestCode(requestCode).get();
        invoice.setConfirmedByRepairerAt(LocalDateTime.now());

        CreateInvoiceResponse response = new CreateInvoiceResponse();
        response.setMessage(CREATE_INVOICE_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ConfirmInvoicePaidResponse> confirmInvoicePaid(ConfirmInvoicePaidRequest request) {
        String requestCode = getRequestCodeNotNull(request.getRequestCode());
        Optional<RepairRequest> optionalRepairRequest = repairRequestDAO.findByRequestCode(requestCode);
        if (optionalRepairRequest.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_REQUEST_CODE);
        }

        RepairRequest repairRequest = optionalRepairRequest.get();
        if (!PaymentMethod.CASH.getId().equals(repairRequest.getPaymentMethodId())) {
            throw new GeneralException(HttpStatus.GONE, CONFIRM_INVOICE_PAID_ONLY_USE_FOR_PAYMENT_IN_CASH);
        }

        if (!PAYMENT_WAITING.getId().equals(repairRequest.getStatusId())) {
            throw new GeneralException(HttpStatus.GONE, CONFIRM_INVOICE_PAID_ONLY_USE_WHEN_STATUS_IS_PAYMENT_WAITING);
        }

        RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();
        Long repairerId = request.getUserId();
        if (!repairRequestMatching.getRepairerId().equals(repairerId)) {
            throw new GeneralException(HttpStatus.GONE, USER_DOES_NOT_HAVE_PERMISSION_TO_CONFIRM_PAID_THIS_INVOICE);

        }

        Repairer repairer = repairerDAO.findByUserId(repairerId).get();

        repairer.setRepairing(false);
        repairRequest.setStatusId(DONE.getId());
        ConfirmInvoicePaidResponse response = new ConfirmInvoicePaidResponse();
        response.setMessage(CONFIRM_INVOICE_PAID_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ConfirmFixingResponse> confirmFixing(ConfirmFixingRequest request) {
        String requestCode = getRequestCodeNotNull(request.getRequestCode());
        Optional<RepairRequest> optionalRepairRequest = repairRequestDAO.findByRequestCode(requestCode);

        if (optionalRepairRequest.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_REQUEST_CODE);
        }

        RepairRequest repairRequest = optionalRepairRequest.get();
        if (!APPROVED.getId().equals(repairRequest.getStatusId())) {
            throw new GeneralException(HttpStatus.GONE, JUST_CAN_CONFIRM_FIXING_WHEN_REQUEST_STATUS_APPROVED);
        }

        RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();
        if (repairRequest.getUserId().equals(repairRequestMatching.getRepairerId())) {
            throw new GeneralException(HttpStatus.GONE, USER_DOES_NOT_HAVE_PERMISSION_TO_CONFIRM_FIXING_THIS_REQUEST);
        }

        repairRequest.setStatusId(FIXING.getId());

        ConfirmFixingResponse response = new ConfirmFixingResponse();
        response.setMessage(CONFIRM_FIXING_SUCCESS);
        response.setStatus(FIXING.name());
        response.setRequestCode(requestCode);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String getRequestCodeNotNull(String requestCode) {
        return requestCode == null ? Strings.EMPTY : requestCode;
    }

    @Override
    public ResponseEntity<RepairerSuggestionResponse> getSuggestionRequestList(RepairerSuggestionRequest request) {
        String type = getRepairerSuggestionTypeValidated(request.getType());

        Long userId = request.getUserId();
        UserAddress userAddress = userAddressDAO.findByUserIdAndIsMainAddressAndDeletedAtIsNull(userId, true).get();
        Long userAddressId = userAddress.getId();

        Repairer repairer = repairerDAO.findByUserId(userId).get();
        Collection<com.fu.flix.entity.Service> services = repairer.getServices();
        List<Long> serviceIds = services.stream()
                .map(com.fu.flix.entity.Service::getId)
                .collect(Collectors.toList());

        List<IRequestingDTO> iRequestingDTOS;
        if (SUGGESTED.name().equals(type)) {
            String districtId = userAddressDAO.findDistrictIdByUserAddressId(userAddressId);
            iRequestingDTOS = repairRequestDAO.findPendingRequestByDistrict(serviceIds, districtId);
        } else {
            String cityId = userAddressDAO.findCityIdByUserAddressId(userAddressId);
            iRequestingDTOS = repairRequestDAO.findPendingRequestByCity(serviceIds, cityId);
        }

        List<RequestingDTO> requestLists = iRequestingDTOS.stream()
                .map(iRequestingDTO -> {
                    RequestingDTO dto = new RequestingDTO();
                    dto.setCustomerName(iRequestingDTO.getCustomerName());
                    dto.setAvatar(iRequestingDTO.getAvatar());
                    dto.setServiceName(iRequestingDTO.getServiceName());
                    dto.setExpectFixingTime(DateFormatUtil.toString(iRequestingDTO.getExpectFixingTime(), DATE_TIME_PATTERN));
                    dto.setAddress(addressService.getAddressFormatted(iRequestingDTO.getAddressId()));
                    dto.setDescription(iRequestingDTO.getDescription());
                    dto.setRequestCode(iRequestingDTO.getRequestCode());
                    dto.setIconImage(iRequestingDTO.getIconImage());
                    dto.setCreatedAt(iRequestingDTO.getCreatedAt());
                    return dto;
                }).collect(Collectors.toList());

        RepairerSuggestionResponse response = new RepairerSuggestionResponse();
        response.setRequestLists(requestLists);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String getRepairerSuggestionTypeValidated(String type) {
        for (RepairerSuggestionType t : RepairerSuggestionType.values()) {
            if (t.name().equals(type)) {
                return type;
            }
        }
        throw new GeneralException(HttpStatus.GONE, INVALID_REPAIRER_SUGGESTION_TYPE);
    }
}
