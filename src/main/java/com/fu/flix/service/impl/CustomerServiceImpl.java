package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.enums.RequestStatus;
import com.fu.flix.constant.enums.RoleType;
import com.fu.flix.dao.*;
import com.fu.flix.dto.*;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.entity.*;
import com.fu.flix.service.AddressService;
import com.fu.flix.service.CustomerService;
import com.fu.flix.service.ValidatorService;
import com.fu.flix.service.VoucherService;
import com.fu.flix.util.DateFormatUtil;
import com.fu.flix.util.InputValidation;
import com.fu.flix.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fu.flix.constant.Constant.*;
import static com.fu.flix.constant.enums.RequestStatus.*;
import static com.fu.flix.constant.enums.TransactionType.PAY_COMMISSIONS;
import static com.fu.flix.constant.enums.TransactionType.REFUNDS;
import static com.fu.flix.constant.enums.VoucherType.INSPECTION;

@Service
@Slf4j
@Transactional
public class CustomerServiceImpl implements CustomerService {
    private final RepairRequestDAO repairRequestDAO;
    private final VoucherDAO voucherDAO;
    private final ServiceDAO serviceDAO;
    private final PaymentMethodDAO paymentMethodDAO;
    private final InvoiceDAO invoiceDAO;
    private final UserAddressDAO userAddressDAO;
    private final CommuneDAO communeDAO;
    private final ImageDAO imageDAO;
    private final CommentDAO commentDAO;
    private final AppConf appConf;
    private final RepairRequestMatchingDAO repairRequestMatchingDAO;
    private final RepairerDAO repairerDAO;
    private final BalanceDAO balanceDAO;
    private final TransactionHistoryDAO transactionHistoryDAO;
    private final StatusDAO statusDAO;
    private final ValidatorService validatorService;
    private final AddressService addressService;
    private final VoucherService voucherService;
    private final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final String DATE_PATTERN = "dd-MM-yyyy";

    public CustomerServiceImpl(RepairRequestDAO repairRequestDAO,
                               VoucherDAO voucherDAO,
                               ServiceDAO serviceDAO,
                               PaymentMethodDAO paymentMethodDAO,
                               InvoiceDAO invoiceDAO,
                               UserAddressDAO userAddressDAO,
                               CommuneDAO communeDAO,
                               ImageDAO imageDAO,
                               CommentDAO commentDAO,
                               AppConf appConf,
                               RepairRequestMatchingDAO repairRequestMatchingDAO,
                               RepairerDAO repairerDAO,
                               BalanceDAO balanceDAO,
                               TransactionHistoryDAO transactionHistoryDAO,
                               StatusDAO statusDAO,
                               ValidatorService validatorService,
                               AddressService addressService,
                               VoucherService voucherService) {
        this.repairRequestDAO = repairRequestDAO;
        this.voucherDAO = voucherDAO;
        this.serviceDAO = serviceDAO;
        this.paymentMethodDAO = paymentMethodDAO;
        this.invoiceDAO = invoiceDAO;
        this.userAddressDAO = userAddressDAO;
        this.communeDAO = communeDAO;
        this.imageDAO = imageDAO;
        this.commentDAO = commentDAO;
        this.appConf = appConf;
        this.repairRequestMatchingDAO = repairRequestMatchingDAO;
        this.repairerDAO = repairerDAO;
        this.balanceDAO = balanceDAO;
        this.transactionHistoryDAO = transactionHistoryDAO;
        this.statusDAO = statusDAO;
        this.validatorService = validatorService;
        this.addressService = addressService;
        this.voucherService = voucherService;
    }

    @Override
    public ResponseEntity<RequestingRepairResponse> createFixingRequest(RequestingRepairRequest request) {
        Long userId = request.getUserId();
        LocalDateTime now = LocalDateTime.now();
        Long voucherId = request.getVoucherId();
        User user = validatorService.getUserValidated(userId);

        if (isHaveAnyPaymentWaitingRequest(userId)) {
            throw new GeneralException(HttpStatus.CONFLICT, CAN_NOT_CREATE_NEW_REQUEST_WHEN_HAVE_OTHER_PAYMENT_WAITING_REQUEST);
        }

        String paymentMethodID = request.getPaymentMethodId();
        Collection<UserVoucher> userVouchers = user.getUserVouchers();

        if (voucherId != null) {
            UsingVoucherDTO usingVoucherDTO = new UsingVoucherDTO(userVouchers, voucherId, request.getServiceId(), paymentMethodID);
            useInspectionVoucher(usingVoucherDTO, now);
        }

        LocalDateTime expectFixingDay = getExpectFixingDay(request, now);

        RepairRequest repairRequest = new RepairRequest();
        repairRequest.setRequestCode(RandomUtil.generateCode());
        repairRequest.setUserId(userId);
        repairRequest.setServiceId(request.getServiceId());
        repairRequest.setPaymentMethodId(getPaymentMethodIdValidated(paymentMethodID));
        repairRequest.setStatusId(PENDING.getId());
        repairRequest.setExpectStartFixingAt(expectFixingDay);
        repairRequest.setDescription(request.getDescription());
        repairRequest.setVoucherId(voucherId);
        repairRequest.setAddressId(getAddressIdValidated(request.getAddressId(), userId));
        repairRequest.setVat(this.appConf.getVat());
        repairRequestDAO.save(repairRequest);

        Invoice invoice = buildInvoice(repairRequest);
        invoiceDAO.save(invoice);

        RequestingRepairResponse response = new RequestingRepairResponse();
        response.setRequestCode(repairRequest.getRequestCode());
        response.setStatus(PENDING.name());
        response.setMessage(CREATE_REPAIR_REQUEST_SUCCESSFUL);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private boolean isHaveAnyPaymentWaitingRequest(Long userId) {
        List<RepairRequest> repairRequests = repairRequestDAO.findByUserIdAndStatusId(userId, PAYMENT_WAITING.getId());
        return repairRequests.size() > 0;
    }

    private Long getAddressIdValidated(Long addressId, Long userId) {
        if (addressId == null || userAddressDAO.findUserAddressToEdit(userId, addressId).isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_ADDRESS);
        }
        return addressId;
    }

    private Invoice buildInvoice(RepairRequest repairRequest) {
        com.fu.flix.entity.Service service = serviceDAO.findById(repairRequest.getServiceId()).get();
        Long inspectionPrice = service.getInspectionPrice();
        Long discount = voucherService.getVoucherDiscount(inspectionPrice, repairRequest.getVoucherId());
        Long beforeVat = inspectionPrice - discount;
        Long vatPrice = (long) (beforeVat * repairRequest.getVat());

        Invoice invoice = new Invoice();
        invoice.setRequestCode(repairRequest.getRequestCode());
        invoice.setInspectionPrice(beforeVat);
        invoice.setTotalPrice(inspectionPrice);
        invoice.setActualProceeds(beforeVat + vatPrice);
        invoice.setVatPrice(vatPrice);
        return invoice;
    }

    private void useInspectionVoucher(UsingVoucherDTO usingVoucherDTO, LocalDateTime now) {
        Long voucherId = usingVoucherDTO.getVoucherId();
        UserVoucher userVoucher = getUserVoucher(usingVoucherDTO.getUserVouchers(), voucherId);
        Long serviceId = usingVoucherDTO.getServiceId();
        if (serviceId == null) {
            throw new GeneralException(HttpStatus.GONE, INVALID_SERVICE);
        }

        Optional<com.fu.flix.entity.Service> optionalService = serviceDAO.findById(serviceId);
        Voucher voucher = voucherDAO.findById(voucherId).get();

        if (!isMatchPaymentMethod(usingVoucherDTO, voucher)) {
            throw new GeneralException(HttpStatus.GONE, PAYMENT_METHOD_NOT_VALID_FOR_THIS_VOUCHER);
        }

        if (optionalService.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_SERVICE);
        }

        if (optionalService.get().getInspectionPrice() < voucher.getMinOrderPrice()) {
            throw new GeneralException(HttpStatus.GONE, INSPECTION_PRICE_MUST_GREATER_OR_EQUAL_VOUCHER_MIN_PRICE);
        }

        if (userVoucher == null) {
            throw new GeneralException(HttpStatus.GONE, USER_NOT_HOLD_VOUCHER);
        }

        if (userVoucher.getQuantity() <= 0) {
            throw new GeneralException(HttpStatus.GONE, USER_NOT_HOLD_VOUCHER);
        }

        if (voucher.getRemainQuantity() <= 0) {
            throw new GeneralException(HttpStatus.CONFLICT, OUT_OF_VOUCHER);
        }

        if (voucher.getExpireDate().isBefore(now)) {
            throw new GeneralException(HttpStatus.CONFLICT, VOUCHER_EXPIRED);
        }

        if (now.isBefore(voucher.getEffectiveDate())) {
            throw new GeneralException(HttpStatus.CONFLICT, VOUCHER_BEFORE_EFFECTIVE_DATE);
        }

        if (!voucher.getType().equals(INSPECTION.name())) {
            throw new GeneralException(HttpStatus.CONFLICT, VOUCHER_MUST_BE_TYPE_INSPECTION);
        }

        voucher.setRemainQuantity(voucher.getRemainQuantity() - 1);
        userVoucher.setQuantity(userVoucher.getQuantity() - 1);
    }

    private boolean isMatchPaymentMethod(UsingVoucherDTO usingVoucherDTO, Voucher voucher) {
        return voucher.getVoucherPaymentMethods().stream()
                .anyMatch(V -> V.getPaymentMethod().getId().equals(usingVoucherDTO.getPaymentMethodId()));
    }

    private LocalDateTime getExpectFixingDay(RequestingRepairRequest request, LocalDateTime now) {
        LocalDateTime expectFixingDay;
        try {
            expectFixingDay = DateFormatUtil.getLocalDateTime(request.getExpectFixingDay(), DATE_TIME_PATTERN);
        } catch (DateTimeParseException e) {
            throw new GeneralException(HttpStatus.GONE, WRONG_LOCAL_DATE_TIME_FORMAT);
        }

        if (isInvalidExpectFixingDay(now, expectFixingDay)) {
            throw new GeneralException(HttpStatus.CONFLICT, EXPECT_FIXING_DAY_MUST_START_AFTER_1_HOURS_AND_BEFORE_30_DAYS);
        }

        return expectFixingDay;
    }

    private boolean isInvalidExpectFixingDay(LocalDateTime now, LocalDateTime expectFixingDay) {
        long minCreateRequestHours = 1L;
        long maxCreateRequestDays = 30L;
        return expectFixingDay.isBefore(now.plusHours(minCreateRequestHours))
                || expectFixingDay.isAfter(now.plusDays(maxCreateRequestDays));
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
    public ResponseEntity<CancelRequestForCustomerResponse> cancelFixingRequest(CancelRequestForCustomerRequest request) {
        String requestCode = getRequestCode(request.getRequestCode());

        RepairRequest repairRequest = getRepairRequest(requestCode);
        if (!repairRequest.getUserId().equals(request.getUserId())) {
            throw new GeneralException(HttpStatus.GONE, USER_DOES_NOT_HAVE_PERMISSION_TO_CANCEL_THIS_REQUEST);
        }

        if (!isCancelable(repairRequest)) {
            throw new GeneralException(HttpStatus.GONE, ONLY_CAN_CANCEL_REQUEST_PENDING_OR_APPROVED);
        }

        if (APPROVED.getId().equals(repairRequest.getStatusId())) {
            updateRepairerAfterCancelRequest(requestCode);
        }

        updateUsedVoucherQuantityAfterCancelRequest(repairRequest);
        updateRepairRequest(request, repairRequest);

        CancelRequestForCustomerResponse response = new CancelRequestForCustomerResponse();
        response.setMessage(CANCEL_REPAIR_REQUEST_SUCCESSFUL);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private boolean isCancelable(RepairRequest repairRequest) {
        String statusId = repairRequest.getStatusId();
        return PENDING.getId().equals(statusId) || APPROVED.getId().equals(statusId);
    }

    @Override
    public void updateRepairerAfterCancelRequest(String requestCode) {
        RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();
        Repairer repairer = repairerDAO.findByUserId(repairRequestMatching.getRepairerId()).get();

        updateRepairerStatus(repairer);
        returnMoneyForRepairer(repairer, requestCode);
    }

    private void updateRepairerStatus(Repairer repairer) {
        repairer.setRepairing(false);
    }

    private void returnMoneyForRepairer(Repairer repairer, String requestCode) {
        TransactionHistory commissionsTransaction = transactionHistoryDAO
                .findByRequestCodeAndType(requestCode, PAY_COMMISSIONS.name()).get();
        Long userId = repairer.getUserId();
        Balance balance = balanceDAO.findByUserId(userId).get();
        Long refunds = commissionsTransaction.getAmount();

        balance.setBalance(balance.getBalance() + refunds);

        TransactionHistory refundsTransaction = new TransactionHistory();
        refundsTransaction.setUserId(userId);
        refundsTransaction.setAmount(refunds);
        refundsTransaction.setType(REFUNDS.name());
        refundsTransaction.setRequestCode(requestCode);
        transactionHistoryDAO.save(refundsTransaction);
    }

    private void updateRepairRequest(CancelRequestForCustomerRequest request, RepairRequest repairRequest) {
        repairRequest.setStatusId(CANCELLED.getId());
        repairRequest.setCancelledByRoleId(RoleType.ROLE_CUSTOMER.getId());
        repairRequest.setReasonCancel(request.getReason());
    }

    @Override
    public void updateUsedVoucherQuantityAfterCancelRequest(RepairRequest repairRequest) {
        Long voucherId = repairRequest.getVoucherId();
        if (voucherId != null) {
            Voucher voucher = voucherDAO.findById(voucherId).get();
            voucher.setRemainQuantity(voucher.getRemainQuantity() + 1);

            User user = validatorService.getUserValidated(repairRequest.getUserId());
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
    public ResponseEntity<HistoryRequestForCustomerResponse> getFixingRequestHistories(HistoryRequestForCustomerRequest request) {
        String statusId = getStatusIdValidated(request.getStatus());
        User user = validatorService.getUserValidated(request.getUsername());
        Status status = statusDAO.findById(statusId).get();

        List<RepairRequest> repairRequests = repairRequestDAO
                .findByUserIdAndStatusIdOrderByCreatedAtDesc(user.getId(), statusId);

        List<HistoryRequestForCustomerDTO> requestHistories = repairRequests.stream()
                .map(repairRequest -> {
                    com.fu.flix.entity.Service service = serviceDAO.findById(repairRequest.getServiceId()).get();
                    String requestCode = repairRequest.getRequestCode();
                    Image image = imageDAO.findById(service.getImageId()).get();

                    HistoryRequestForCustomerDTO dto = new HistoryRequestForCustomerDTO();
                    dto.setRequestCode(requestCode);
                    dto.setStatus(status.getName());
                    dto.setImage(image.getUrl());
                    dto.setServiceId(service.getId());
                    dto.setServiceName(service.getName());
                    dto.setDescription(repairRequest.getDescription());
                    dto.setPrice(getRequestPrice(requestCode, false));
                    dto.setActualPrice(getRequestPrice(requestCode, true));
                    dto.setDate(DateFormatUtil.toString(repairRequest.getCreatedAt(), DATE_TIME_PATTERN));

                    return dto;
                }).collect(Collectors.toList());

        HistoryRequestForCustomerResponse response = new HistoryRequestForCustomerResponse();
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
    public ResponseEntity<RequestingDetailForCustomerResponse> getDetailFixingRequest(RequestingDetailForCustomerRequest request) {
        String requestCode = getRequestCode(request.getRequestCode());
        Long customerId = request.getUserId();
        IDetailFixingRequestDTO dto = repairRequestDAO.findDetailFixingRequest(customerId, requestCode);

        RequestingDetailForCustomerResponse response = new RequestingDetailForCustomerResponse();
        if (dto != null) {
            VoucherDTO voucherDTO = voucherService.getVoucherInfo(dto.getVoucherId());
            response.setStatus(dto.getStatus());
            response.setServiceImage(dto.getServiceImage());
            response.setServiceId(dto.getServiceId());
            response.setServiceName(dto.getServiceName());
            response.setCustomerAddress(addressService.getAddressFormatted(dto.getCustomerAddressId()));
            response.setCustomerPhone(dto.getCustomerPhone());
            response.setCustomerName(dto.getCustomerName());
            response.setExpectFixingDay(DateFormatUtil.toString(dto.getExpectFixingDay(), DATE_TIME_PATTERN));
            response.setRequestDescription(dto.getRequestDescription());
            response.setVoucherDescription(voucherDTO.getVoucherDescription());
            response.setVoucherDiscount(voucherDTO.getVoucherDiscount());
            response.setPaymentMethod(dto.getPaymentMethod());
            response.setDate(DateFormatUtil.toString(dto.getCreatedAt(), DATE_TIME_PATTERN));
            response.setPrice(dto.getPrice());
            response.setActualPrice(dto.getActualPrice());
            response.setVatPrice(dto.getVatPrice());
            response.setRequestCode(requestCode);
            response.setRepairerAddress(addressService.getAddressFormatted(dto.getRepairerAddressId()));
            response.setRepairerPhone(dto.getRepairerPhone());
            response.setRepairerName(dto.getRepairerName());
            response.setRepairerId(dto.getRepairerId());
            response.setRepairerAvatar(dto.getRepairerAvatar());
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String getRequestCode(String requestCode) {
        return requestCode == null ? Strings.EMPTY : requestCode;
    }

    @Override
    public RepairRequest getRepairRequest(String requestCode) {
        Optional<RepairRequest> optionalRepairRequest = repairRequestDAO.findByRequestCode(requestCode);
        if (optionalRepairRequest.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_REQUEST_CODE);
        }
        return optionalRepairRequest.get();
    }

    private Long getRequestPrice(String requestCode, boolean isActualPrice) {
        Optional<Invoice> optionalInvoice = invoiceDAO.findByRequestCode(requestCode);
        if (optionalInvoice.isPresent()) {
            Invoice invoice = optionalInvoice.get();
            return isActualPrice ? invoice.getActualProceeds() : invoice.getTotalPrice();
        }
        return 0L;
    }

    @Override
    public ResponseEntity<MainAddressResponse> getMainAddress(MainAddressRequest request) {
        User user = validatorService.getUserValidated(request.getUsername());
        UserAddress userAddress = userAddressDAO.findByUserIdAndIsMainAddressAndDeletedAtIsNull(user.getId(), true).get();
        Long addressId = userAddress.getId();

        MainAddressResponse response = new MainAddressResponse();
        response.setAddressId(addressId);
        response.setCustomerName(user.getFullName());
        response.setPhone(userAddress.getPhone());
        response.setAddressName(addressService.getAddressFormatted(addressId));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UserAddressResponse> getCustomerAddresses(UserAddressRequest request) {
        User user = validatorService.getUserValidated(request.getUsername());
        List<UserAddress> userAddresses = userAddressDAO.findByUserIdAndDeletedAtIsNull(user.getId());

        List<UserAddressDTO> addresses = userAddresses.stream()
                .map(userAddress -> {
                    Long addressId = userAddress.getId();
                    UserAddressDTO dto = new UserAddressDTO();
                    dto.setAddressId(addressId);
                    dto.setCustomerName(user.getFullName());
                    dto.setPhone(userAddress.getPhone());
                    dto.setAddressName(addressService.getAddressFormatted(addressId));
                    dto.setMainAddress(userAddress.isMainAddress());
                    return dto;
                }).collect(Collectors.toList());

        UserAddressResponse response = new UserAddressResponse();
        response.setAddresses(addresses);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<DeleteAddressResponse> deleteCustomerAddress(DeleteAddressRequest request) {
        Long addressId = request.getAddressId();
        if (addressId == null) {
            throw new GeneralException(HttpStatus.GONE, INVALID_ADDRESS);
        }

        Optional<UserAddress> optionalUserAddress = userAddressDAO
                .findUserAddressToDelete(request.getUserId(), addressId);

        optionalUserAddress.ifPresent(userAddress -> userAddress.setDeletedAt(LocalDateTime.now()));

        DeleteAddressResponse response = new DeleteAddressResponse();
        response.setMessage(DELETE_ADDRESS_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EditAddressResponse> editCustomerAddress(EditAddressRequest request) {
        Long addressId = request.getAddressId();
        if (addressId == null) {
            throw new GeneralException(HttpStatus.GONE, INVALID_ADDRESS);
        }

        String phone = request.getPhone();
        if (!InputValidation.isPhoneValid(phone)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_PHONE_NUMBER);
        }

        String communeId = request.getCommuneId();
        if (communeId == null) {
            throw new GeneralException(HttpStatus.GONE, INVALID_COMMUNE);
        }

        String streetAddress = request.getStreetAddress();
        if (streetAddress == null || streetAddress.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, STREET_ADDRESS_IS_REQUIRED);
        }

        Optional<UserAddress> optionalUserAddress = userAddressDAO
                .findUserAddressToEdit(request.getUserId(), addressId);
        Optional<Commune> optionalCommune = communeDAO.findById(communeId);

        if (optionalUserAddress.isPresent() && optionalUserAddress.isPresent()) {
            UserAddress userAddress = optionalUserAddress.get();
            Commune commune = optionalCommune.get();

            userAddress.setName(request.getName());
            userAddress.setPhone(phone);
            userAddress.setCommuneId(commune.getId());
            userAddress.setStreetAddress(streetAddress);
        }

        EditAddressResponse response = new EditAddressResponse();
        response.setAddressId(addressId);
        response.setMessage(EDIT_ADDRESS_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CreateAddressResponse> createCustomerAddress(CreateAddressRequest request) {
        String communeId = request.getCommuneId();
        if (communeId == null) {
            throw new GeneralException(HttpStatus.GONE, INVALID_COMMUNE);
        }

        Optional<Commune> optionalCommune = communeDAO.findById(communeId);
        if (optionalCommune.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_COMMUNE);
        }

        String phone = request.getPhone();
        if (!InputValidation.isPhoneValid(phone)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_PHONE_NUMBER);
        }

        String streetAddress = request.getStreetAddress();
        if (streetAddress == null || streetAddress.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, STREET_ADDRESS_IS_REQUIRED);
        }

        Commune commune = optionalCommune.get();

        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(request.getUserId());
        userAddress.setMainAddress(false);
        userAddress.setStreetAddress(streetAddress);
        userAddress.setName(request.getFullName());
        userAddress.setPhone(phone);
        userAddress.setCommuneId(commune.getId());
        UserAddress savedUserAddress = userAddressDAO.save(userAddress);

        CreateAddressResponse response = new CreateAddressResponse();
        response.setAddressId(savedUserAddress.getId());
        response.setMessage(CREATE_ADDRESS_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CustomerProfileResponse> getCustomerProfile(CustomerProfileRequest request) {
        User user = validatorService.getUserValidated(request.getUsername());
        Image image = imageDAO.findById(user.getAvatar()).get();
        String dob = user.getDateOfBirth() == null
                ? null
                : DateFormatUtil.toString(user.getDateOfBirth(), DATE_PATTERN);

        CustomerProfileResponse response = new CustomerProfileResponse();
        response.setPhone(user.getPhone());
        response.setFullName(user.getFullName());
        response.setAvatarUrl(image.getUrl());
        response.setDateOfBirth(dob);
        response.setGender(user.getGender());
        response.setEmail(user.getEmail());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UpdateCustomerProfileResponse> updateCustomerProfile(UpdateCustomerProfileRequest request) {
        String fullName = request.getFullName();
        if (fullName == null || fullName.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, FULL_NAME_IS_REQUIRED);
        }

        String email = request.getEmail();
        if (!InputValidation.isEmailValid(email)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_EMAIL);
        }

        LocalDate dob;
        try {
            dob = DateFormatUtil.getLocalDate(request.getDateOfBirth(), DATE_PATTERN);
        } catch (DateTimeParseException e) {
            throw new GeneralException(HttpStatus.GONE, WRONG_LOCAL_DATE_FORMAT);
        }

        User user = validatorService.getUserValidated(request.getUsername());
        user.setFullName(fullName);
        user.setDateOfBirth(dob);
        user.setGender(request.isGender());
        user.setEmail(email);

        UpdateCustomerProfileResponse response = new UpdateCustomerProfileResponse();
        response.setMessage(UPDATED_PROFILE_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<RepairerProfileResponse> getRepairerProfile(RepairerProfileRequest request) {
        RepairerProfileResponse response = new RepairerProfileResponse();
        Long repairerId = request.getRepairerId();
        if (repairerId != null) {
            IRepairerProfileDTO repairerProfile = commentDAO.findRepairerProfile(repairerId);
            ISuccessfulRepairDTO successfulRepair = commentDAO.findSuccessfulRepair(repairerId);
            response.setJointAt(repairerProfile.getJoinAt());
            response.setSuccessfulRepair(successfulRepair.getSuccessfulRepair());
            response.setRepairerName(repairerProfile.getRepairerName());
            response.setRating(repairerProfile.getRating());
            response.setExperience(repairerProfile.getExperience());
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<RepairerCommentResponse> getRepairerComments(RepairerCommentRequest request) {
        Long repairerId = request.getRepairerId();
        RepairerCommentResponse response = new RepairerCommentResponse();

        if (repairerId != null) {
            Integer offset = request.getOffset() == null
                    ? this.appConf.getOffsetDefault()
                    : request.getOffset();

            Integer limit = request.getLimit() == null
                    ? this.appConf.getLimitQueryDefault()
                    : request.getLimit();

            List<IRepairerCommentDTO> repairComments = commentDAO.findRepairComments(repairerId, limit, offset);
            List<RepairerCommentDTO> repairerCommentDTOs = repairComments.stream()
                    .map(rc -> {
                        RepairerCommentDTO dto = new RepairerCommentDTO();
                        dto.setComment(rc.getComment());
                        dto.setCustomerId(rc.getCustomerId());
                        dto.setRating(rc.getRating());
                        dto.setCustomerName(rc.getCustomerName());
                        return dto;
                    }).collect(Collectors.toList());
            response.setRepairerComments(repairerCommentDTOs);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ChooseMainAddressResponse> chooseMainAddress(ChooseMainAddressRequest request) {
        Long userId = request.getUserId();
        Long addressId = request.getAddressId();
        if (addressId == null) {
            throw new GeneralException(HttpStatus.GONE, ADDRESS_ID_IS_REQUIRED);
        }

        Optional<UserAddress> optionalSelectedAddress = userAddressDAO
                .findUserAddressToEdit(userId, addressId);

        Optional<UserAddress> optionalMainAddress = userAddressDAO.findByUserIdAndIsMainAddressAndDeletedAtIsNull(userId, true);

        if (optionalSelectedAddress.isPresent() && optionalMainAddress.isPresent()) {
            optionalMainAddress.get().setMainAddress(false);
            optionalSelectedAddress.get().setMainAddress(true);
        }

        ChooseMainAddressResponse response = new ChooseMainAddressResponse();
        response.setMessage(CHOOSING_MAIN_ADDRESS_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
