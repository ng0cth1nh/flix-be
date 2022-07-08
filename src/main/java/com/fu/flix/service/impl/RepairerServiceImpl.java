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
import com.fu.flix.service.*;
import com.fu.flix.util.DateFormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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
    private final InvoiceDAO invoiceDAO;
    private final BalanceDAO balanceDAO;
    private final AppConf appConf;
    private final TransactionHistoryDAO transactionHistoryDAO;
    private final CustomerService customerService;
    private final UserAddressDAO userAddressDAO;
    private final AddressService addressService;
    private final VoucherService voucherService;
    private final SubServiceDAO subServiceDAO;
    private final RequestService requestService;
    private final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final String DATE_PATTERN = "dd-MM-yyyy";

    public RepairerServiceImpl(RepairerDAO repairerDAO,
                               RepairRequestDAO repairRequestDAO,
                               RepairRequestMatchingDAO repairRequestMatchingDAO,
                               InvoiceDAO invoiceDAO,
                               BalanceDAO balanceDAO,
                               AppConf appConf,
                               TransactionHistoryDAO transactionHistoryDAO,
                               CustomerService customerService,
                               UserAddressDAO userAddressDAO,
                               AddressService addressService,
                               VoucherService voucherService,
                               SubServiceDAO subServiceDAO,
                               RequestService requestService) {
        this.repairerDAO = repairerDAO;
        this.repairRequestDAO = repairRequestDAO;
        this.repairRequestMatchingDAO = repairRequestMatchingDAO;

        this.invoiceDAO = invoiceDAO;
        this.balanceDAO = balanceDAO;
        this.appConf = appConf;
        this.transactionHistoryDAO = transactionHistoryDAO;
        this.customerService = customerService;
        this.userAddressDAO = userAddressDAO;
        this.addressService = addressService;
        this.voucherService = voucherService;
        this.subServiceDAO = subServiceDAO;
        this.requestService = requestService;
    }

    @Override
    public ResponseEntity<RepairerApproveResponse> approveRequest(RepairerApproveRequest request) {
        String requestCode = request.getRequestCode();
        RepairRequest repairRequest = requestService.getRepairRequest(requestCode);

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
        String requestCode = request.getRequestCode();
        if (requestService.isEmptyRequestCode(requestCode)) {
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
            response.setCustomerAddress(addressService.getAddressFormatted(dto.getAddressId()));
            response.setCustomerPhone(dto.getCustomerPhone());
            response.setCustomerName(dto.getCustomerName());
            response.setExpectFixingTime(DateFormatUtil.toString(dto.getExpectFixingTime(), DATE_TIME_PATTERN));
            response.setRequestDescription(dto.getRequestDescription());
            response.setVoucherDescription(voucherDTO.getVoucherDescription());
            response.setVoucherDiscount(voucherDTO.getVoucherDiscount());
            response.setPaymentMethod(dto.getPaymentMethod());
            response.setDate(DateFormatUtil.toString(dto.getCreatedAt(), DATE_TIME_PATTERN));
            response.setPrice(dto.getPrice());
            response.setActualPrice(dto.getActualPrice());
            response.setVatPrice(dto.getVatPrice());
            response.setRequestCode(requestCode);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CancelRequestForRepairerResponse> cancelFixingRequest(CancelRequestForRepairerRequest request) {
        String requestCode = request.getRequestCode();
        if (requestService.isEmptyRequestCode(requestCode)) {
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
        String requestCode = request.getRequestCode();
        RepairRequest repairRequest = requestService.getRepairRequest(requestCode);

        RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();
        if (!request.getUserId().equals(repairRequestMatching.getRepairerId())) {
            throw new GeneralException(HttpStatus.GONE, REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_CREATE_INVOICE_FOR_THIS_REQUEST);
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

    @Override
    public ResponseEntity<RequestingSuggestionResponse> getSuggestionRequestList(RequestingSuggestionRequest request) {
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

        List<RequestingDTO> requestLists = getRequestList(iRequestingDTOS);

        RequestingSuggestionResponse response = new RequestingSuggestionResponse();
        response.setRequestList(requestLists);

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

    @Override
    public ResponseEntity<RequestingFilterResponse> getFilterRequestList(RequestingFilterRequest request) {
        List<Long> serviceIds = request.getServiceIds();
        if (serviceIds == null || serviceIds.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, SERVICE_IDS_ARE_REQUIRED);
        }

        LocalDateTime start = getFilterDateValidated(request.getStartDate()).atTime(LocalTime.MIN);
        LocalDateTime end = getFilterDateValidated(request.getEndDate()).atTime(LocalTime.MAX);

        String locationType = getLocationTypeValidated(request.getLocationType());
        String locationId = request.getLocationId();
        List<IRequestingDTO> iRequestingDTOS;
        switch (LocationType.valueOf(locationType)) {
            case CITY:
                iRequestingDTOS = repairRequestDAO.filterPendingRequestByCity(serviceIds, locationId, start, end);
                break;
            case DISTRICT:
                iRequestingDTOS = repairRequestDAO.filterPendingRequestByDistrict(serviceIds, locationId, start, end);
                break;
            default:
                iRequestingDTOS = repairRequestDAO.filterPendingRequestByCommune(serviceIds, locationId, start, end);
                break;
        }

        List<RequestingDTO> requestLists = getRequestList(iRequestingDTOS);
        RequestingFilterResponse response = new RequestingFilterResponse();
        response.setRequestList(requestLists);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private LocalDate getFilterDateValidated(String strDate) {
        if (strDate == null) {
            throw new GeneralException(HttpStatus.GONE, START_DATE_AND_END_DATE_ARE_REQUIRED);
        }

        try {
            return DateFormatUtil.getLocalDate(strDate, DATE_PATTERN);
        } catch (DateTimeParseException e) {
            throw new GeneralException(HttpStatus.GONE, WRONG_LOCAL_DATE_FORMAT);
        }
    }

    private List<RequestingDTO> getRequestList(List<IRequestingDTO> iRequestingDTOS) {
        return iRequestingDTOS.stream()
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
    }

    private String getLocationTypeValidated(String type) {
        for (LocationType t : LocationType.values()) {
            if (t.name().equals(type)) {
                return type;
            }
        }
        throw new GeneralException(HttpStatus.GONE, INVALID_LOCATION_TYPE);
    }

    @Override
    public ResponseEntity<AddSubServicesToInvoiceResponse> addSubServicesToInvoice(AddSubServicesToInvoiceRequest request) {
        List<Long> subServiceIds = request.getSubServiceIds();
        if (subServiceIds == null || subServiceIds.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, SUB_SERVICE_ID_IS_REQUIRED);
        }

        String requestCode = request.getRequestCode();
        RepairRequest repairRequest = requestService.getRepairRequest(requestCode);

        RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();
        if (!request.getUserId().equals(repairRequestMatching.getRepairerId())) {
            throw new GeneralException(HttpStatus.GONE, REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_ADD_SUB_SERVICES_FOR_THIS_INVOICE);
        }

        Collection<SubService> subServices = subServiceDAO.findSubServices(subServiceIds, repairRequest.getServiceId());
        if (subServices.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_SUB_SERVICE_ID);
        }

        Invoice invoice = invoiceDAO.findByRequestCode(requestCode).get();
        Long totalSubServicePrice = subServices.stream().mapToLong(SubService::getPrice).sum();

        invoice.setTotalSubServicePrice(totalSubServicePrice);
        invoice.getSubServices().addAll(subServices);
        updateCommonInvoiceMoney(invoice, totalSubServicePrice, repairRequest.getVat());

        AddSubServicesToInvoiceResponse response = new AddSubServicesToInvoiceResponse();
        response.setMessage(ADD_SUB_SERVICE_TO_INVOICE_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void updateCommonInvoiceMoney(Invoice invoice, Long addingMoney, Double vat) {
        long newTotalPrice = invoice.getTotalPrice() + addingMoney;
        long newTotalDiscount = voucherService.getVoucherDiscount(newTotalPrice, invoice.getVoucherId());
        long beforeVat = newTotalPrice - newTotalDiscount;
        long newVatPrice = (long) (beforeVat * vat);

        invoice.setTotalPrice(newTotalPrice);
        invoice.setTotalDiscount(newTotalDiscount);
        invoice.setVatPrice(newVatPrice);
        invoice.setActualProceeds(beforeVat + newVatPrice);
    }
}
