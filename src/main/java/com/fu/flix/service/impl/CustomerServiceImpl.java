package com.fu.flix.service.impl;

import com.fu.flix.dao.RepairRequestDAO;
import com.fu.flix.dao.UserDAO;
import com.fu.flix.dao.VoucherDAO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.CancelRequestingRepairRequest;
import com.fu.flix.dto.request.RequestingRepairRequest;
import com.fu.flix.dto.response.CancelRequestingRepairResponse;
import com.fu.flix.dto.response.RequestingRepairResponse;
import com.fu.flix.entity.*;
import com.fu.flix.service.CustomerService;
import com.fu.flix.util.DateFormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static com.fu.flix.constant.Constant.*;
import static com.fu.flix.constant.enums.Status.*;
import static com.fu.flix.constant.enums.VoucherType.INSPECTION;

@Service
@Slf4j
@Transactional
public class CustomerServiceImpl implements CustomerService {
    private final UserDAO userDAO;
    private final RepairRequestDAO repairRequestDAO;
    private final VoucherDAO voucherDAO;
    private final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public CustomerServiceImpl(UserDAO userDAO,
                               RepairRequestDAO repairRequestDAO,
                               VoucherDAO voucherDAO) {
        this.userDAO = userDAO;
        this.repairRequestDAO = repairRequestDAO;
        this.voucherDAO = voucherDAO;
    }

    @Override
    public ResponseEntity<RequestingRepairResponse> createFixingRequest(RequestingRepairRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Long voucherId = request.getVoucherId();
        User user = userDAO.findByUsername(request.getUsername()).get();
        Collection<UserVoucher> userVouchers = user.getUserVouchers();
        useVoucher(userVouchers, voucherId, now);

        Long userId = user.getId();

        LocalDateTime expectFixingDay = getExpectFixingDay(request, now);

        RepairRequest repairRequest = new RepairRequest();
        repairRequest.setRequestCode(UUID.randomUUID().toString());
        repairRequest.setUserId(userId);
        repairRequest.setServiceId(request.getServiceId());
        repairRequest.setPaymentMethodId(request.getPaymentMethodId());
        repairRequest.setStatusId(PENDING.getId());
        repairRequest.setExpectStartFixingAt(expectFixingDay);
        repairRequest.setDescription(request.getDescription());
        repairRequest.setVoucherId(voucherId);
        repairRequest.setAddressId(request.getAddressId());
        repairRequest.setCreatedAt(now);
        repairRequest.setUpdatedAt(now);
        repairRequestDAO.save(repairRequest);

        RequestingRepairResponse response = new RequestingRepairResponse();
        response.setRequestCode(repairRequest.getRequestCode());
        response.setStatus(PENDING.name());
        response.setMessage(CREATE_REPAIR_REQUEST_SUCCESSFUL);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void useVoucher(Collection<UserVoucher> userVouchers, Long voucherId, LocalDateTime now) {
        UserVoucher userVoucher = getUserVoucher(userVouchers, voucherId);

        if (userVoucher == null) {
            throw new GeneralException(USER_NOT_HOLD_VOUCHER);
        }

        if (userVoucher.getQuantity() <= 0) {
            throw new GeneralException(USER_NOT_HOLD_VOUCHER);
        }

        Voucher voucher = voucherDAO.findById(voucherId).get();

        if (voucher.getRemainQuantity() <= 0) {
            throw new GeneralException(OUT_OF_VOUCHER);
        }

        if (voucher.getExpireDate().isBefore(now)) {
            throw new GeneralException(VOUCHER_EXPIRED);
        }

        if (now.isBefore(voucher.getEffectiveDate())) {
            throw new GeneralException(VOUCHER_BEFORE_EFFECTIVE_DATE);
        }

        if (!voucher.getType().equals(INSPECTION.name())) {
            throw new GeneralException(VOUCHER_MUST_BE_TYPE_INSPECTION);
        }

        voucher.setRemainQuantity(voucher.getRemainQuantity() - 1);
        userVoucher.setQuantity(userVoucher.getQuantity() - 1);
    }

    private UserVoucher getUserVoucher(Collection<UserVoucher> userVouchers, Long voucherId) {
        return userVouchers.stream()
                .filter(uv -> uv.getUserVoucherId().getVoucherId().equals(voucherId))
                .findFirst()
                .orElse(null);
    }

    private LocalDateTime getExpectFixingDay(RequestingRepairRequest request, LocalDateTime now) {
        LocalDateTime expectFixingDay;
        try {
            expectFixingDay = DateFormatUtil.getLocalDateTime(request.getExpectFixingDay(), DATE_TIME_PATTERN);
        } catch (DateTimeParseException e) {
            throw new GeneralException(WRONG_LOCAL_DATE_TIME_FORMAT);
        }

        if (expectFixingDay.isBefore(now)) {
            throw new GeneralException(EXPECT_FIXING_DAY_MUST_GREATER_OR_EQUAL_NOW);
        }

        return expectFixingDay;
    }

    @Override
    public ResponseEntity<CancelRequestingRepairResponse> cancelFixingRequest(CancelRequestingRepairRequest request) {
        RepairRequest repairRequest = getRepairRequestValidated(request.getRequestCode(), request.getUsername());

        LocalDateTime now = LocalDateTime.now();
        repairRequest.setStatusId(CANCELLED.getId());
        repairRequest.setUpdatedAt(now);

        CancelRequestingRepairResponse response = new CancelRequestingRepairResponse();
        response.setMessage(CANCEL_REPAIR_REQUEST_SUCCESSFUL);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private RepairRequest getRepairRequestValidated(String requestCode, String username) {
        Optional<RepairRequest> optionalRepairRequest = repairRequestDAO.findByRequestCode(requestCode);
        if (optionalRepairRequest.isEmpty()) {
            throw new GeneralException(INVALID_REQUEST_CODE);
        }

        User user = userDAO.findByUsername(username).get();
        RepairRequest repairRequest = optionalRepairRequest.get();

        if (!repairRequest.getUserId().equals(user.getId())) {
            throw new GeneralException(INVALID_REQUEST_CODE);
        }

        if (!isCancelable(repairRequest)) {
            throw new GeneralException(ONLY_CAN_CANCEL_REQUEST_PENDING_OR_ACCEPTED);
        }

        return repairRequest;
    }

    private boolean isCancelable(RepairRequest repairRequest) {
        String statusId = repairRequest.getStatusId();
        return PENDING.getId().equals(statusId) ||
                ACCEPTED.getId().equals(statusId);
    }
}
