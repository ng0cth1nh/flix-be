package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.enums.*;
import com.fu.flix.constant.enums.PaymentMethod;
import com.fu.flix.dao.*;
import com.fu.flix.dto.*;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.entity.*;
import com.fu.flix.job.CronJob;
import com.fu.flix.service.*;
import com.fu.flix.util.DateFormatUtil;
import com.fu.flix.util.InputValidation;
import com.fu.flix.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fu.flix.constant.Constant.*;
import static com.fu.flix.constant.enums.RequestStatus.*;
import static com.fu.flix.constant.enums.TransactionStatus.PENDING;
import static com.fu.flix.constant.enums.TransactionType.*;
import static com.fu.flix.constant.enums.TransactionStatus.SUCCESS;

@Service
@Slf4j
@Transactional
public class RepairerServiceImpl implements RepairerService {
    private final RepairerDAO repairerDAO;
    private final RepairRequestDAO repairRequestDAO;
    private final RepairRequestMatchingDAO repairRequestMatchingDAO;
    private final InvoiceDAO invoiceDAO;
    private final BalanceDAO balanceDAO;
    private final FinanceService financeService;
    private final AppConf appConf;
    private final TransactionHistoryDAO transactionHistoryDAO;
    private final AddressService addressService;
    private final CronJob cronJob;
    private final UserAddressDAO userAddressDAO;
    private final ValidatorService validatorService;
    private final VoucherService voucherService;
    private final SubServiceDAO subServiceDAO;
    private final RequestService requestService;
    private final FCMService fcmService;
    private final WithdrawRequestDAO withdrawRequestDAO;
    private final BankInfoDAO bankInfoDAO;
    private final AccessoryDAO accessoryDAO;
    private final ExtraServiceDAO extraServiceDAO;
    private final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final Long DESCRIPTION_MAX_LENGTH;

    public RepairerServiceImpl(RepairerDAO repairerDAO,
                               RepairRequestDAO repairRequestDAO,
                               RepairRequestMatchingDAO repairRequestMatchingDAO,
                               InvoiceDAO invoiceDAO,
                               BalanceDAO balanceDAO,
                               FinanceService financeService,
                               AppConf appConf,
                               TransactionHistoryDAO transactionHistoryDAO,
                               AddressService addressService,
                               CronJob cronJob,
                               UserAddressDAO userAddressDAO,
                               ValidatorService validatorService,
                               VoucherService voucherService,
                               SubServiceDAO subServiceDAO,
                               RequestService requestService,
                               FCMService fcmService,
                               AccessoryDAO accessoryDAO,
                               ExtraServiceDAO extraServiceDAO,
                               WithdrawRequestDAO withdrawRequestDAO,
                               BankInfoDAO bankInfoDAO) {
        this.repairerDAO = repairerDAO;
        this.repairRequestDAO = repairRequestDAO;
        this.repairRequestMatchingDAO = repairRequestMatchingDAO;

        this.invoiceDAO = invoiceDAO;
        this.balanceDAO = balanceDAO;
        this.financeService = financeService;
        this.appConf = appConf;
        this.transactionHistoryDAO = transactionHistoryDAO;
        this.addressService = addressService;
        this.cronJob = cronJob;
        this.userAddressDAO = userAddressDAO;
        this.validatorService = validatorService;
        this.voucherService = voucherService;
        this.subServiceDAO = subServiceDAO;
        this.requestService = requestService;
        this.fcmService = fcmService;
        this.withdrawRequestDAO = withdrawRequestDAO;
        this.bankInfoDAO = bankInfoDAO;
        this.accessoryDAO = accessoryDAO;
        this.extraServiceDAO = extraServiceDAO;
        this.DESCRIPTION_MAX_LENGTH = appConf.getDescriptionMaxLength();
    }

    @Override
    public ResponseEntity<RepairerApproveResponse> approveRequest(RepairerApproveRequest request) {
        String requestCode = request.getRequestCode();
        RepairRequest repairRequest = requestService.getRepairRequest(requestCode);

        if (!RequestStatus.PENDING.getId().equals(repairRequest.getStatusId())) {
            throw new GeneralException(HttpStatus.CONFLICT, JUST_CAN_ACCEPT_PENDING_REQUEST);
        }

        Long repairerId = request.getUserId();
        Repairer repairer = repairerDAO.findByUserId(repairerId).get();

        if (repairer.isRepairing()) {
            throw new GeneralException(HttpStatus.CONFLICT, CAN_NOT_ACCEPT_REQUEST_WHEN_ON_ANOTHER_FIXING);
        }

        Balance balance = balanceDAO.findByUserId(repairerId).get();
        Long milestoneMoney = this.appConf.getMilestoneMoney();
        if (balance.getBalance() < milestoneMoney) {
            throw new GeneralException(HttpStatus.CONFLICT, BALANCE_MUST_GREATER_THAN_OR_EQUAL_ + milestoneMoney);
        }

        repairRequest.setStatusId(APPROVED.getId());

        RepairRequestMatching repairRequestMatching = buildRepairRequestMatching(requestCode, repairerId);
        repairRequestMatchingDAO.save(repairRequestMatching);

        UserAddress userAddress = userAddressDAO.findByUserIdAndIsMainAddressAndDeletedAtIsNull(repairerId, true).get();
        Invoice invoice = invoiceDAO.findByRequestCode(requestCode).get();
        invoice.setRepairerAddress(addressService.getAddressFormatted(userAddress.getId()));
        invoice.setRepairerPhone(userAddress.getPhone());
        invoice.setRepairerName(userAddress.getName());

        UserNotificationDTO customerNotificationDTO = new UserNotificationDTO(
                "request",
                NotificationStatus.REQUEST_APPROVED.name(),
                repairRequest.getUserId(),
                NotificationType.REQUEST_APPROVED.name(),
                null,
                requestCode);
        UserNotificationDTO repairerNotificationDTO = new UserNotificationDTO(
                "request",
                NotificationStatus.REQUEST_APPROVED.name(),
                repairerId,
                NotificationType.REQUEST_APPROVED.name(),
                null,
                requestCode);

        fcmService.sendAndSaveNotification(customerNotificationDTO, requestCode);
        fcmService.sendAndSaveNotification(repairerNotificationDTO, requestCode);

        RepairerApproveResponse response = new RepairerApproveResponse();
        response.setMessage(APPROVAL_REQUEST_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private RepairRequestMatching buildRepairRequestMatching(String requestCode, Long repairerId) {
        RepairRequestMatching repairRequestMatching = new RepairRequestMatching();
        repairRequestMatching.setRequestCode(requestCode);
        repairRequestMatching.setRepairerId(repairerId);
        return repairRequestMatching;
    }

    @Override
    public ResponseEntity<RequestingDetailForRepairerResponse> getRepairRequestDetail(RequestingDetailForRepairerRequest request) {
        String requestCode = request.getRequestCode();
        if (Strings.isEmpty(requestCode)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_REQUEST_CODE);
        }

        RequestingDetailForRepairerResponse response = new RequestingDetailForRepairerResponse();
        IDetailFixingRequestForRepairerDTO dto = repairRequestDAO.findDetailFixingRequestForRepairer(request.getUserId(), requestCode);

        if (dto != null) {
            VoucherDTO voucherDTO = voucherService.getVoucherInfo(dto.getVoucherId());
            response.setStatus(dto.getStatus());
            response.setServiceImage(dto.getServiceImage());
            response.setServiceId(dto.getServiceId());
            response.setServiceName(dto.getServiceName());
            response.setCustomerId(dto.getCustomerId());
            response.setAvatar(dto.getAvatar());
            response.setCustomerAddress(dto.getAddress());
            response.setCustomerPhone(dto.getCustomerPhone());
            response.setCustomerName(dto.getCustomerName());
            response.setExpectFixingTime(DateFormatUtil.toString(dto.getExpectFixingTime(), DATE_TIME_PATTERN));
            response.setVoucherDescription(voucherDTO.getVoucherDescription());
            response.setRequestDescription(dto.getRequestDescription());
            response.setVoucherDiscount(voucherDTO.getVoucherDiscount());
            response.setPaymentMethod(dto.getPaymentMethod());
            response.setDate(DateFormatUtil.toString(dto.getCreatedAt(), DATE_TIME_PATTERN));
            response.setTotalPrice(dto.getTotalPrice());
            response.setActualPrice(dto.getActualPrice());
            response.setVatPrice(dto.getVatPrice());
            response.setRequestCode(requestCode);
            response.setInspectionPrice(dto.getInspectionPrice());
            response.setTotalDiscount(dto.getTotalDiscount());
            response.setApprovedTime(
                    dto.getApprovedTime() == null
                            ? null
                            : DateFormatUtil.toString(dto.getApprovedTime(), DATE_TIME_PATTERN)
            );
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CancelRequestForRepairerResponse> cancelFixingRequest(CancelRequestForRepairerRequest request) {
        String requestCode = request.getRequestCode();
        if (Strings.isEmpty(requestCode)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_REQUEST_CODE);
        }

        Optional<RepairRequest> optionalRepairRequest = repairRequestDAO.findByRequestCode(requestCode);
        if (optionalRepairRequest.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_REQUEST_CODE);
        }

        RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();
        if (!repairRequestMatching.getRepairerId().equals(request.getUserId())) {
            throw new GeneralException(HttpStatus.GONE, USER_DOES_NOT_HAVE_PERMISSION_TO_CANCEL_THIS_REQUEST);
        }

        RepairRequest repairRequest = optionalRepairRequest.get();
        if (!isCancelable(repairRequest)) {
            throw new GeneralException(HttpStatus.GONE, ONLY_CAN_CANCEL_REQUEST_FIXING_OR_APPROVED);
        }

        if (FIXING.getId().equals(repairRequest.getStatusId())) {
            cronJob.updateRepairerAfterCancelFixingRequest(requestCode);
        }

        if (isFined(repairRequest)) {
            cronJob.monetaryFine(request.getUserId(), requestCode);
        }

        cronJob.refundVoucher(repairRequest);
        cronJob.updateRequestAfterCancel(request.getReason(), RoleType.ROLE_REPAIRER.getId(), repairRequest);

        UserNotificationDTO userNotificationDTO = new UserNotificationDTO(
                "request",
                NotificationStatus.REQUEST_CANCELED.name(),
                repairRequest.getUserId(),
                NotificationType.REQUEST_CANCELED.name(),
                null,
                requestCode
        );
        fcmService.sendAndSaveNotification(userNotificationDTO, requestCode);

        CancelRequestForRepairerResponse response = new CancelRequestForRepairerResponse();
        response.setMessage(CANCEL_REPAIR_REQUEST_SUCCESSFUL);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    private boolean isCancelable(RepairRequest repairRequest) {
        String statusId = repairRequest.getStatusId();
        return APPROVED.getId().equals(statusId) || FIXING.getId().equals(statusId);
    }

    private boolean isFined(RepairRequest repairRequest) {
        return cronJob.isOnRequestCancelTime(repairRequest.getExpectStartFixingAt())
                && APPROVED.getId().equals(repairRequest.getStatusId());
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
                    dto.setServiceId(h.getServiceId());

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
        String requestCode = request.getRequestCode();
        RepairRequest repairRequest = requestService.getRepairRequest(requestCode);

        RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();
        Long repairerId = request.getUserId();
        if (!repairerId.equals(repairRequestMatching.getRepairerId())) {
            throw new GeneralException(HttpStatus.GONE, REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_CREATE_INVOICE_FOR_THIS_REQUEST);
        }

        repairRequest.setStatusId(PAYMENT_WAITING.getId());

        Invoice invoice = invoiceDAO.findByRequestCode(requestCode).get();
        invoice.setConfirmedByRepairerAt(LocalDateTime.now());

        if (isCanNotApplyVoucherToInvoice(invoice)) {
            cronJob.refundVoucher(repairRequest);
        }

        Balance balance = balanceDAO.findByUserId(repairerId).get();
        Long commission = financeService.getCommission(invoice);
        long requiredMoney = commission + appConf.getMilestoneMoney();
        if (balance.getBalance() < requiredMoney) {
            throw new GeneralException(HttpStatus.CONFLICT, BALANCE_MUST_GREATER_THAN_OR_EQUAL_ + requiredMoney);
        }

        minusCommissions(balance, commission, invoice.getRequestCode());

        UserNotificationDTO customerNotificationDTO = new UserNotificationDTO(
                "request",
                NotificationStatus.CREATE_INVOICE.name(),
                repairRequest.getUserId(),
                NotificationType.CREATE_INVOICE.name(),
                null,
                requestCode
        );
        UserNotificationDTO repairerNotificationDTO = new UserNotificationDTO(
                "request",
                NotificationStatus.CREATE_INVOICE.name(),
                repairerId,
                NotificationType.CREATE_INVOICE.name(),
                null,
                requestCode
        );

        fcmService.sendAndSaveNotification(customerNotificationDTO, requestCode);
        fcmService.sendAndSaveNotification(repairerNotificationDTO, requestCode);

        CreateInvoiceResponse response = new CreateInvoiceResponse();
        response.setMessage(CREATE_INVOICE_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void minusCommissions(Balance balance, Long commission, String requestCode) {
        balance.setBalance(balance.getBalance() - commission);
        TransactionHistory transactionHistory = new TransactionHistory();
        transactionHistory.setUserId(balance.getUserId());
        transactionHistory.setAmount(commission);
        transactionHistory.setType(PAY_COMMISSIONS.name());
        transactionHistory.setRequestCode(requestCode);
        transactionHistory.setStatus(SUCCESS.name());
        transactionHistory.setTransactionCode(RandomUtil.generateCode());
        transactionHistoryDAO.save(transactionHistory);
    }

    private boolean isCanNotApplyVoucherToInvoice(Invoice invoice) {
        Long voucherId = invoice.getVoucherId();
        return voucherId != null && invoice.getTotalPrice() < voucherService.getVoucherMinOrderPrice(voucherId);
    }

    @Override
    public ResponseEntity<ConfirmInvoicePaidResponse> confirmInvoicePaid(ConfirmInvoicePaidRequest request) {
        String requestCode = request.getRequestCode();
        RepairRequest repairRequest = requestService.getRepairRequest(requestCode);

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

        UserNotificationDTO customerNotificationDTO = new UserNotificationDTO(
                "request",
                NotificationStatus.REQUEST_DONE.name(),
                repairRequest.getUserId(),
                NotificationType.REQUEST_DONE.name(),
                null,
                requestCode
        );
        UserNotificationDTO repairerNotificationDTO = new UserNotificationDTO(
                "request",
                NotificationStatus.REQUEST_DONE.name(),
                repairerId,
                NotificationType.REQUEST_DONE.name(),
                null,
                requestCode
        );

        fcmService.sendAndSaveNotification(customerNotificationDTO, requestCode);
        fcmService.sendAndSaveNotification(repairerNotificationDTO, requestCode);

        ConfirmInvoicePaidResponse response = new ConfirmInvoicePaidResponse();
        response.setMessage(CONFIRM_INVOICE_PAID_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ConfirmFixingResponse> confirmFixing(ConfirmFixingRequest request) {
        String requestCode = request.getRequestCode();
        RepairRequest repairRequest = requestService.getRepairRequest(requestCode);

        if (!APPROVED.getId().equals(repairRequest.getStatusId())) {
            throw new GeneralException(HttpStatus.GONE, JUST_CAN_CONFIRM_FIXING_WHEN_REQUEST_STATUS_APPROVED);
        }

        RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();
        Long repairerId = request.getUserId();
        if (!repairerId.equals(repairRequestMatching.getRepairerId())) {
            throw new GeneralException(HttpStatus.GONE, USER_DOES_NOT_HAVE_PERMISSION_TO_CONFIRM_FIXING_THIS_REQUEST);
        }

        Repairer repairer = repairerDAO.findByUserId(repairerId).get();
        if (repairer.isRepairing()) {
            throw new GeneralException(HttpStatus.CONFLICT, CAN_NOT_CONFIRM_FIXING_WHEN_ON_ANOTHER_FIXING);
        }

        Invoice invoice = invoiceDAO.findByRequestCode(requestCode).get();
        invoice.setConfirmFixingAt(LocalDateTime.now());

        repairRequest.setStatusId(FIXING.getId());
        repairer.setRepairing(true);

        UserNotificationDTO customerNotificationDTO = new UserNotificationDTO(
                "request",
                NotificationStatus.REQUEST_CONFIRM_FIXING.name(),
                repairRequest.getUserId(),
                NotificationType.REQUEST_CONFIRM_FIXING.name(),
                null,
                requestCode
        );
        UserNotificationDTO repairerNotificationDTO = new UserNotificationDTO(
                "request",
                NotificationStatus.REQUEST_CONFIRM_FIXING.name(),
                repairerId,
                NotificationType.REQUEST_CONFIRM_FIXING.name(),
                null,
                requestCode
        );

        fcmService.sendAndSaveNotification(customerNotificationDTO, requestCode);
        fcmService.sendAndSaveNotification(repairerNotificationDTO, requestCode);

        ConfirmFixingResponse response = new ConfirmFixingResponse();
        response.setMessage(CONFIRM_FIXING_SUCCESS);
        response.setStatus(FIXING.name());
        response.setRequestCode(requestCode);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AddSubServicesToInvoiceResponse> putSubServicesToInvoice(AddSubServicesToInvoiceRequest request) {
        String requestCode = request.getRequestCode();
        RepairRequest repairRequest = requestService.getRepairRequest(requestCode);
        if (!FIXING.getId().equals(repairRequest.getStatusId())) {
            throw new GeneralException(HttpStatus.GONE, JUST_CAN_ADD_SUB_SERVICES_WHEN_REQUEST_STATUS_IS_FIXING);
        }

        RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();
        if (!request.getUserId().equals(repairRequestMatching.getRepairerId())) {
            throw new GeneralException(HttpStatus.GONE, REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_ADD_SUB_SERVICES_FOR_THIS_INVOICE);
        }

        List<Long> subServiceIds = request.getSubServiceIds();
        Collection<SubService> subServices = subServiceDAO.findSubServices(subServiceIds, repairRequest.getServiceId());

        Invoice invoice = invoiceDAO.findByRequestCode(requestCode).get();
        Double vat = repairRequest.getVat();

        Collection<SubService> oldSubServices = invoice.getSubServices();

        oldSubServices.clear();
        long minusMoney = invoice.getTotalSubServicePrice();
        invoice.setTotalSubServicePrice(0L);
        minusCommonInvoiceMoney(invoice, minusMoney, vat);

        oldSubServices.addAll(subServices);
        long plusMoney = subServices.stream().mapToLong(SubService::getPrice).sum();
        invoice.setTotalSubServicePrice(plusMoney);
        plusCommonInvoiceMoney(invoice, plusMoney, vat);

        AddSubServicesToInvoiceResponse response = new AddSubServicesToInvoiceResponse();
        response.setMessage(PUT_SUB_SERVICE_TO_INVOICE_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AddAccessoriesToInvoiceResponse> putAccessoriesToInvoice(AddAccessoriesToInvoiceRequest request) {
        String requestCode = request.getRequestCode();
        RepairRequest repairRequest = requestService.getRepairRequest(requestCode);
        if (!FIXING.getId().equals(repairRequest.getStatusId())) {
            throw new GeneralException(HttpStatus.GONE, JUST_CAN_ADD_ACCESSORIES_WHEN_REQUEST_STATUS_IS_FIXING);
        }

        RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();
        if (!request.getUserId().equals(repairRequestMatching.getRepairerId())) {
            throw new GeneralException(HttpStatus.GONE, REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_ADD_ACCESSORIES_FOR_THIS_INVOICE);
        }

        List<Long> accessoryIds = request.getAccessoryIds();
        Collection<Accessory> accessories = accessoryDAO.findAccessories(accessoryIds, repairRequest.getServiceId());

        Invoice invoice = invoiceDAO.findByRequestCode(requestCode).get();
        Double vat = repairRequest.getVat();

        Collection<Accessory> oldAccessories = invoice.getAccessories();

        oldAccessories.clear();
        long minusMoney = invoice.getTotalAccessoryPrice();
        invoice.setTotalAccessoryPrice(0L);
        minusCommonInvoiceMoney(invoice, minusMoney, vat);

        oldAccessories.addAll(accessories);
        long plusMoney = accessories.stream().mapToLong(Accessory::getPrice).sum();
        invoice.setTotalAccessoryPrice(plusMoney);
        plusCommonInvoiceMoney(invoice, plusMoney, vat);

        AddAccessoriesToInvoiceResponse response = new AddAccessoriesToInvoiceResponse();
        response.setMessage(PUT_ACCESSORIES_TO_INVOICE_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AddExtraServiceToInvoiceResponse> putExtraServicesToInvoice(AddExtraServiceToInvoiceRequest request) {
        Collection<ExtraServiceInputDTO> extraServiceInputDTOS = request.getExtraServices() == null
                ? new ArrayList<>()
                : request.getExtraServices();

        if (isInvalidExtraServices(extraServiceInputDTOS)) {
            throw new GeneralException(HttpStatus.GONE, LIST_EXTRA_SERVICES_CONTAIN_INVALID_ELEMENT);
        }

        String requestCode = request.getRequestCode();
        RepairRequest repairRequest = requestService.getRepairRequest(requestCode);
        if (!FIXING.getId().equals(repairRequest.getStatusId())) {
            throw new GeneralException(HttpStatus.GONE, JUST_CAN_ADD_EXTRA_SERVICE_WHEN_REQUEST_STATUS_IS_FIXING);
        }

        RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();
        if (!request.getUserId().equals(repairRequestMatching.getRepairerId())) {
            throw new GeneralException(HttpStatus.GONE, REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_ADD_EXTRA_SERVICE_FOR_THIS_INVOICE);
        }

        Invoice invoice = invoiceDAO.findByRequestCode(requestCode).get();
        Double vat = repairRequest.getVat();

        extraServiceDAO.deleteAllByRequestCode(requestCode);
        long minusMoney = invoice.getTotalExtraServicePrice();
        invoice.setTotalExtraServicePrice(0L);
        minusCommonInvoiceMoney(invoice, minusMoney, vat);

        long plusMoney = saveAndReturnExtraServicesTotalPrice(extraServiceInputDTOS, requestCode);
        invoice.setTotalExtraServicePrice(plusMoney);
        plusCommonInvoiceMoney(invoice, plusMoney, vat);

        AddExtraServiceToInvoiceResponse response = new AddExtraServiceToInvoiceResponse();
        response.setMessage(PUT_EXTRA_SERVICE_TO_INVOICE_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private boolean isInvalidExtraServices(Collection<ExtraServiceInputDTO> extraServicesInput) {
        if (CollectionUtils.isEmpty(extraServicesInput)) {
            return false;
        }
        return extraServicesInput.stream().anyMatch(this::isInvalidExtraService);
    }

    private boolean isInvalidExtraService(ExtraServiceInputDTO extraServiceInputDTO) {
        if (Strings.isEmpty(extraServiceInputDTO.getName())) {
            return true;
        }

        if (!InputValidation.isDescriptionValid(extraServiceInputDTO.getDescription(), DESCRIPTION_MAX_LENGTH)) {
            return true;
        }

        Long price = extraServiceInputDTO.getPrice();
        if (price == null || price < 0) {
            return true;
        }

        Integer insuranceTime = extraServiceInputDTO.getInsuranceTime();
        return insuranceTime != null && insuranceTime < 0;
    }

    private long saveAndReturnExtraServicesTotalPrice(Collection<ExtraServiceInputDTO> extraServiceInputDTOS, String requestCode) {
        long totalExtraServicePrice = 0L;
        Collection<ExtraService> extraServices = new ArrayList<>();

        for (ExtraServiceInputDTO dto : extraServiceInputDTOS) {
            long price = dto.getPrice();
            totalExtraServicePrice += price;

            ExtraService extraService = new ExtraService();
            extraService.setName(dto.getName());
            extraService.setPrice(price);
            extraService.setDescription(dto.getDescription());
            extraService.setRequestCode(requestCode);
            extraService.setInsuranceTime(dto.getInsuranceTime());
            extraServices.add(extraService);
        }

        extraServiceDAO.saveAll(extraServices);
        return totalExtraServicePrice;
    }

    private void minusCommonInvoiceMoney(Invoice invoice, Long minusMoney, Double vat) {
        long newTotalPrice = invoice.getTotalPrice() - minusMoney;
        updateCommonInvoiceMoney(invoice, vat, newTotalPrice);
    }

    private void plusCommonInvoiceMoney(Invoice invoice, Long plusMoney, Double vat) {
        long newTotalPrice = invoice.getTotalPrice() + plusMoney;
        updateCommonInvoiceMoney(invoice, vat, newTotalPrice);
    }

    private void updateCommonInvoiceMoney(Invoice invoice, Double vat, long newTotalPrice) {
        long newTotalDiscount = voucherService.getVoucherDiscount(newTotalPrice, invoice.getVoucherId());
        long beforeVat = newTotalPrice - newTotalDiscount;
        long newVatPrice = (long) (newTotalPrice * vat);

        invoice.setTotalPrice(newTotalPrice);
        invoice.setTotalDiscount(newTotalDiscount);
        invoice.setVatPrice(newVatPrice);
        invoice.setActualProceeds(beforeVat + newVatPrice);
        invoice.setProfit(financeService.getProfit(invoice));
    }

    @Override
    public ResponseEntity<RepairerWithdrawResponse> requestWithdraw(RepairerWithdrawRequest request) {
        Long amount = request.getAmount();
        if (amount == null || amount < appConf.getMinVnPay()) {
            throw new GeneralException(HttpStatus.GONE, AMOUNT_MUST_BE_GREATER_OR_EQUAL_ + appConf.getMinVnPay());
        }

        Long repairerId = request.getUserId();
        Balance balance = balanceDAO.findByUserId(repairerId).get();

        if (repairRequestMatchingDAO.isRepairerHavingAnyRequest(repairerId) && balance.getBalance() - amount < appConf.getMilestoneMoney()) {
            throw new GeneralException(HttpStatus.GONE, BALANCE_MUST_GREATER_THAN_OR_EQUAL_ + appConf.getMilestoneMoney());
        } else if (balance.getBalance() < amount) {
            throw new GeneralException(HttpStatus.GONE, BALANCE_NOT_ENOUGH);
        }

        String withdrawType = getWithdrawTypeValidated(request.getWithdrawType());
        boolean isNullable = WithdrawType.CASH.name().equals(withdrawType);
        validatedBankInfo(request, isNullable);

        WithdrawRequest withdrawRequest = new WithdrawRequest();
        withdrawRequest.setType(withdrawType);
        withdrawRequest.setBankCode(request.getBankCode());
        withdrawRequest.setBankAccountNumber(request.getBankAccountNumber());
        withdrawRequest.setBankAccountName(request.getBankAccountName());
        WithdrawRequest savedWithdrawRequest = withdrawRequestDAO.save(withdrawRequest);

        TransactionHistory transactionHistory = new TransactionHistory();
        transactionHistory.setTransactionCode(RandomUtil.generateCode());
        transactionHistory.setAmount(amount);
        transactionHistory.setType(WITHDRAW.name());
        transactionHistory.setUserId(repairerId);
        transactionHistory.setStatus(PENDING.name());
        transactionHistory.setWithdrawRequestId(savedWithdrawRequest.getId());
        TransactionHistory savedTransactionHistory = transactionHistoryDAO.save(transactionHistory);

        RepairerWithdrawResponse response = new RepairerWithdrawResponse();
        response.setMessage(CREATE_REQUEST_WITHDRAW_SUCCESS);
        response.setTransactionId(savedTransactionHistory.getId());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String getWithdrawTypeValidated(String type) {
        for (WithdrawType wt : WithdrawType.values()) {
            if (wt.name().equals(type)) {
                return type;
            }
        }
        throw new GeneralException(HttpStatus.GONE, INVALID_WITHDRAW_TYPE);
    }

    private void validatedBankInfo(RepairerWithdrawRequest request, boolean isNullable) {
        if (!InputValidation.isBankNameValid(request.getBankAccountName(), isNullable)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_BANK_ACCOUNT_NAME);
        } else if (!InputValidation.isBankNumberValid(request.getBankAccountNumber(), isNullable)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_BANK_ACCOUNT_NUMBER);
        } else if (isInvalidBankCode(request.getBankCode(), isNullable)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_BANK_CODE);
        }
    }

    private boolean isInvalidBankCode(String bankCode, boolean isNullable) {
        if (bankCode == null && isNullable) {
            return false;
        } else if (bankCode == null) {
            return true;
        }
        return bankInfoDAO.findById(bankCode).isEmpty();
    }

    @Override
    public ResponseEntity<RepairerTransactionsResponse> getTransactionHistories(RepairerTransactionsRequest request) {
        int pageSize = validatorService.getPageSize(request.getPageSize());
        int pageNumber = validatorService.getPageNumber(request.getPageNumber());
        int offset = pageNumber * pageSize;

        Long repairerId = request.getUserId();
        List<TransactionHistory> transactionDTOs = transactionHistoryDAO
                .findTransactionsForRepairer(repairerId, pageSize, offset);
        long totalRecord = transactionHistoryDAO.countByUserId(repairerId);

        List<RepairerTransactionDTO> transactions = transactionDTOs.stream()
                .map(transaction -> {
                    RepairerTransactionDTO dto = new RepairerTransactionDTO();
                    dto.setId(transaction.getId());
                    dto.setAmount(transaction.getAmount());
                    dto.setTransactionCode(transaction.getTransactionCode());
                    dto.setType(transaction.getType());
                    dto.setStatus(transaction.getStatus());
                    dto.setCreatedAt(DateFormatUtil.toString(transaction.getCreatedAt(), DATE_TIME_PATTERN));
                    dto.setTransactionId(transaction.getId());
                    dto.setRequestCode(transaction.getRequestCode());
                    return dto;
                })
                .collect(Collectors.toList());

        RepairerTransactionsResponse response = new RepairerTransactionsResponse();
        response.setTransactions(transactions);
        response.setTotalRecord(totalRecord);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
