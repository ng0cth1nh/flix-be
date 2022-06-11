package com.fu.flix.service.impl;

import com.fu.flix.dao.*;
import com.fu.flix.dto.HistoryRepairRequestDTO;
import com.fu.flix.dto.UsingVoucherDTO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.CancelRequestingRepairRequest;
import com.fu.flix.dto.request.HistoryRequestingRepairRequest;
import com.fu.flix.dto.request.RequestingRepairRequest;
import com.fu.flix.dto.response.CancelRequestingRepairResponse;
import com.fu.flix.dto.response.HistoryRequestingRepairResponse;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final ServiceDAO serviceDAO;
    private final PaymentMethodDAO paymentMethodDAO;
    private final InvoiceDAO invoiceDAO;
    private final DiscountPercentDAO discountPercentDAO;
    private final DiscountMoneyDAO discountMoneyDAO;
    private final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public CustomerServiceImpl(UserDAO userDAO,
                               RepairRequestDAO repairRequestDAO,
                               VoucherDAO voucherDAO,
                               ServiceDAO serviceDAO,
                               PaymentMethodDAO paymentMethodDAO,
                               InvoiceDAO invoiceDAO,
                               DiscountPercentDAO discountPercentDAO,
                               DiscountMoneyDAO discountMoneyDAO) {
        this.userDAO = userDAO;
        this.repairRequestDAO = repairRequestDAO;
        this.voucherDAO = voucherDAO;
        this.serviceDAO = serviceDAO;
        this.paymentMethodDAO = paymentMethodDAO;
        this.invoiceDAO = invoiceDAO;
        this.discountPercentDAO = discountPercentDAO;
        this.discountMoneyDAO = discountMoneyDAO;
    }

    @Override
    public ResponseEntity<RequestingRepairResponse> createFixingRequest(RequestingRepairRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Long voucherId = request.getVoucherId();
        User user = userDAO.findByUsername(request.getUsername()).get();
        Collection<UserVoucher> userVouchers = user.getUserVouchers();

        UsingVoucherDTO usingVoucherDTO = new UsingVoucherDTO(userVouchers, voucherId, request.getServiceId());
        useInspectionVoucher(usingVoucherDTO, now);

        Long userId = user.getId();

        LocalDateTime expectFixingDay = getExpectFixingDay(request, now);

        RepairRequest repairRequest = new RepairRequest();
        repairRequest.setRequestCode(UUID.randomUUID().toString());
        repairRequest.setUserId(userId);
        repairRequest.setServiceId(request.getServiceId());
        repairRequest.setPaymentMethodId(getPaymentMethodIdValidated(request.getPaymentMethodId()));
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

    private void useInspectionVoucher(UsingVoucherDTO usingVoucherDTO, LocalDateTime now) {
        Long voucherId = usingVoucherDTO.getVoucherId();
        UserVoucher userVoucher = getUserVoucher(usingVoucherDTO.getUserVouchers(), voucherId);
        Optional<com.fu.flix.entity.Service> optionalService = serviceDAO.findById(usingVoucherDTO.getServiceId());
        Voucher voucher = voucherDAO.findById(voucherId).get();

        if (optionalService.isEmpty()) {
            throw new GeneralException(INVALID_SERVICE);
        }

        if (optionalService.get().getInspectionPrice() < voucher.getMinOrderPrice()) {
            throw new GeneralException(INSPECTION_PRICE_MUST_GREATER_OR_EQUAL_VOUCHER_MIN_PRICE);
        }

        if (userVoucher == null) {
            throw new GeneralException(USER_NOT_HOLD_VOUCHER);
        }

        if (userVoucher.getQuantity() <= 0) {
            throw new GeneralException(USER_NOT_HOLD_VOUCHER);
        }

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

    private String getPaymentMethodIdValidated(String paymentMethodId) {
        String cashID = com.fu.flix.constant.enums.PaymentMethod.CASH.getId();
        if (paymentMethodId == null) {
            return cashID;
        }

        Optional<PaymentMethod> optionalPaymentMethod = paymentMethodDAO.findById(paymentMethodId);
        return optionalPaymentMethod.isPresent()
                ? paymentMethodId
                : cashID;
    }

    @Override
    public ResponseEntity<CancelRequestingRepairResponse> cancelFixingRequest(CancelRequestingRepairRequest request) {
        RepairRequest repairRequest = getRepairRequestValidated(request.getRequestCode(), request.getUsername());

        LocalDateTime now = LocalDateTime.now();

        updateUsedVoucherQuantity(repairRequest);

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
            throw new GeneralException(ONLY_CAN_CANCEL_REQUEST_PENDING_OR_CONFIRMED);
        }

        return repairRequest;
    }

    private boolean isCancelable(RepairRequest repairRequest) {
        String statusId = repairRequest.getStatusId();
        return PENDING.getId().equals(statusId) ||
                CONFIRMED.getId().equals(statusId);
    }

    private void updateUsedVoucherQuantity(RepairRequest repairRequest) {
        Long voucherId = repairRequest.getVoucherId();
        if (voucherId != null) {
            Voucher voucher = voucherDAO.findById(voucherId).get();
            voucher.setRemainQuantity(voucher.getRemainQuantity() + 1);

            User user = userDAO.findById(repairRequest.getUserId()).get();
            Collection<UserVoucher> userVouchers = user.getUserVouchers();
            UserVoucher userVoucher = getUserVoucher(userVouchers, voucherId);
            userVoucher.setQuantity(userVoucher.getQuantity() + 1);
        }
    }

    private UserVoucher getUserVoucher(Collection<UserVoucher> userVouchers, Long voucherId) {
        return userVouchers.stream()
                .filter(uv -> uv.getUserVoucherId().getVoucherId().equals(voucherId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public ResponseEntity<HistoryRequestingRepairResponse> getFixingRequestHistories(HistoryRequestingRepairRequest request) {
        User user = userDAO.findByUsername(request.getUsername()).get();
        List<RepairRequest> repairRequests = repairRequestDAO
                .findByUserIdAndStatusId(user.getId(), getStatusIdValidated(request.getStatus()));

        List<HistoryRepairRequestDTO> historyRepairRequestDTOS = repairRequests.stream()
                .map(repairRequest -> {
                    com.fu.flix.entity.Service service = serviceDAO.findById(repairRequest.getServiceId()).get();

                    HistoryRepairRequestDTO dto = new HistoryRepairRequestDTO();
                    dto.setRequestCode(repairRequest.getRequestCode());
                    dto.setServiceName(service.getName());
                    dto.setDescription(repairRequest.getDescription());
                    dto.setPrice(getRepairRequestPrice(repairRequest, service.getInspectionPrice()));

                    return dto;
                }).collect(Collectors.toList());

        HistoryRequestingRepairResponse response = new HistoryRequestingRepairResponse();
        response.setRequestHistories(historyRepairRequestDTOS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String getStatusIdValidated(String status) {
        try {
            return valueOf(status).getId();
        } catch (IllegalArgumentException e) {
            throw new GeneralException(INVALID_STATUS);
        }
    }

    private Double getRepairRequestPrice(RepairRequest repairRequest, Double inspectionPrice) {
        Optional<Invoice> optionalInvoice = invoiceDAO.findByRepairRequestId(repairRequest.getId());
        if (optionalInvoice.isPresent()) {
            return optionalInvoice.get().getActualProceeds();
        }

        Double discount = getVoucherDiscount(inspectionPrice, repairRequest.getVoucherId());
        return inspectionPrice - discount;
    }

    private Double getVoucherDiscount(Double inspectionPrice, Long voucherId) {
        if (voucherId == null) {
            return 0.0;
        }

        Voucher voucher = voucherDAO.findById(voucherId).get();
        if (voucher.isDiscountMoney()) {
            DiscountMoney discountMoney = discountMoneyDAO.findByVoucherId(voucherId).get();
            return discountMoney.getDiscountMoney();
        }

        DiscountPercent discountPercent = discountPercentDAO.findByVoucherId(voucherId).get();
        return discountPercent.getDiscountPercent() * inspectionPrice;
    }
}
