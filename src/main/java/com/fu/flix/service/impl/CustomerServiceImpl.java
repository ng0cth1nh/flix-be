package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.enums.RoleType;
import com.fu.flix.dao.*;
import com.fu.flix.dto.*;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.entity.*;
import com.fu.flix.service.CustomerService;
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
import static com.fu.flix.constant.enums.Status.*;
import static com.fu.flix.constant.enums.TransactionType.PAY_COMMISSIONS;
import static com.fu.flix.constant.enums.TransactionType.REFUNDS;
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
    private final UserAddressDAO userAddressDAO;
    private final CommuneDAO communeDAO;
    private final DistrictDAO districtDAO;
    private final CityDAO cityDAO;
    private final ImageDAO imageDAO;
    private final CommentDAO commentDAO;
    private final AppConf appConf;
    private final RepairRequestMatchingDAO repairRequestMatchingDAO;
    private final RepairerDAO repairerDAO;
    private final BalanceDAO balanceDAO;
    private final TransactionHistoryDAO transactionHistoryDAO;
    private final String COMMA = ", ";
    private final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final String DATE_PATTERN = "dd-MM-yyyy";

    public CustomerServiceImpl(UserDAO userDAO,
                               RepairRequestDAO repairRequestDAO,
                               VoucherDAO voucherDAO,
                               ServiceDAO serviceDAO,
                               PaymentMethodDAO paymentMethodDAO,
                               InvoiceDAO invoiceDAO,
                               DiscountPercentDAO discountPercentDAO,
                               DiscountMoneyDAO discountMoneyDAO,
                               UserAddressDAO userAddressDAO,
                               CommuneDAO communeDAO,
                               DistrictDAO districtDAO,
                               CityDAO cityDAO,
                               ImageDAO imageDAO,
                               CommentDAO commentDAO,
                               AppConf appConf,
                               RepairRequestMatchingDAO repairRequestMatchingDAO,
                               RepairerDAO repairerDAO,
                               BalanceDAO balanceDAO,
                               TransactionHistoryDAO transactionHistoryDAO) {
        this.userDAO = userDAO;
        this.repairRequestDAO = repairRequestDAO;
        this.voucherDAO = voucherDAO;
        this.serviceDAO = serviceDAO;
        this.paymentMethodDAO = paymentMethodDAO;
        this.invoiceDAO = invoiceDAO;
        this.discountPercentDAO = discountPercentDAO;
        this.discountMoneyDAO = discountMoneyDAO;
        this.userAddressDAO = userAddressDAO;
        this.communeDAO = communeDAO;
        this.districtDAO = districtDAO;
        this.cityDAO = cityDAO;
        this.imageDAO = imageDAO;
        this.commentDAO = commentDAO;
        this.appConf = appConf;
        this.repairRequestMatchingDAO = repairRequestMatchingDAO;
        this.repairerDAO = repairerDAO;
        this.balanceDAO = balanceDAO;
        this.transactionHistoryDAO = transactionHistoryDAO;
    }

    @Override
    public ResponseEntity<RequestingRepairResponse> createFixingRequest(RequestingRepairRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Long voucherId = request.getVoucherId();
        User user = userDAO.findByUsername(request.getUsername()).get();
        String paymentMethodID = request.getPaymentMethodId();
        Collection<UserVoucher> userVouchers = user.getUserVouchers();

        if (voucherId != null) {
            UsingVoucherDTO usingVoucherDTO = new UsingVoucherDTO(userVouchers, voucherId, request.getServiceId(), paymentMethodID);
            useInspectionVoucher(usingVoucherDTO, now);
        }

        Long userId = user.getId();

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
        repairRequest.setAddressId(request.getAddressId());
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

    private Invoice buildInvoice(RepairRequest repairRequest) {
        com.fu.flix.entity.Service service = serviceDAO.findById(repairRequest.getServiceId()).get();
        Double inspectionPrice = service.getInspectionPrice();
        Double discount = getVoucherDiscount(inspectionPrice, repairRequest.getVoucherId());
        Double beforeVat = inspectionPrice - discount;

        Invoice invoice = new Invoice();
        invoice.setRequestCode(repairRequest.getRequestCode());
        invoice.setInspectionPrice(beforeVat);
        invoice.setTotalPrice(beforeVat);
        invoice.setActualProceeds(beforeVat + beforeVat * repairRequest.getVat());
        return invoice;
    }

    private Double getVoucherDiscount(Double inspectionPrice, Long voucherId) {
        double discount = 0.0;
        if (voucherId == null) {
            return discount;
        }

        Voucher voucher = voucherDAO.findById(voucherId).get();
        if (voucher.isDiscountMoney()) {
            DiscountMoney discountMoney = discountMoneyDAO.findByVoucherId(voucherId).get();
            return discountMoney.getDiscountMoney();
        }

        DiscountPercent discountPercent = discountPercentDAO.findByVoucherId(voucherId).get();
        discount = discountPercent.getDiscountPercent() * inspectionPrice;
        return discount > discountPercent.getMaxDiscountPrice()
                ? discountPercent.getMaxDiscountPrice()
                : discount;
    }

    private void useInspectionVoucher(UsingVoucherDTO usingVoucherDTO, LocalDateTime now) {
        Long voucherId = usingVoucherDTO.getVoucherId();
        UserVoucher userVoucher = getUserVoucher(usingVoucherDTO.getUserVouchers(), voucherId);
        Optional<com.fu.flix.entity.Service> optionalService = serviceDAO.findById(usingVoucherDTO.getServiceId());
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
        Balance balance = balanceDAO.findByUserId(repairer.getUserId()).get();
        Double refunds = commissionsTransaction.getAmount();

        balance.setBalance(balance.getBalance() + refunds);

        TransactionHistory refundsTransaction = new TransactionHistory();
        refundsTransaction.setBalanceId(balance.getId());
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
                    String requestCode = repairRequest.getRequestCode();

                    HistoryRepairRequestDTO dto = new HistoryRepairRequestDTO();
                    dto.setRequestCode(requestCode);
                    dto.setServiceName(service.getName());
                    dto.setDescription(repairRequest.getDescription());
                    dto.setPrice(getRepairRequestPrice(requestCode));
                    dto.setDate(DateFormatUtil.toString(repairRequest.getCreatedAt(), DATE_TIME_PATTERN));

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
            throw new GeneralException(HttpStatus.GONE, INVALID_STATUS);
        }
    }

    @Override
    public ResponseEntity<RequestingDetailForCustomerResponse> getDetailFixingRequest(RequestingDetailForCustomerRequest request) {
        String requestCode = getRequestCode(request.getRequestCode());
        RepairRequest repairRequest = getRepairRequest(requestCode);
        if (!repairRequest.getUserId().equals(request.getUserId())) {
            throw new GeneralException(HttpStatus.GONE, USER_DOES_NOT_HAVE_PERMISSION_TO_SEE_THIS_REQUEST);
        }

        com.fu.flix.entity.Service service = serviceDAO.findById(repairRequest.getServiceId()).get();

        RequestingDetailForCustomerResponse response = new RequestingDetailForCustomerResponse();
        response.setServiceId(service.getId());
        response.setServiceName(service.getName());
        response.setAddressId(repairRequest.getAddressId());
        response.setExpectFixingDay(DateFormatUtil.toString(repairRequest.getExpectStartFixingAt(), DATE_TIME_PATTERN));
        response.setDescription(repairRequest.getDescription());
        response.setVoucherId(repairRequest.getVoucherId());
        response.setPaymentMethodId(repairRequest.getPaymentMethodId());
        response.setDate(DateFormatUtil.toString(repairRequest.getCreatedAt(), DATE_TIME_PATTERN));
        response.setPrice(getRepairRequestPrice(requestCode));
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

    private Double getRepairRequestPrice(String requestCode) {
        Optional<Invoice> optionalInvoice = invoiceDAO.findByRequestCode(requestCode);
        if (optionalInvoice.isPresent()) {
            return optionalInvoice.get().getActualProceeds();
        }

        return 0.0;
    }

    @Override
    public ResponseEntity<MainAddressResponse> getMainAddress(MainAddressRequest request) {
        User user = userDAO.findByUsername(request.getUsername()).get();
        UserAddress userAddress = userAddressDAO.findByUserIdAndIsMainAddressAndDeletedAtIsNull(user.getId(), true).get();

        MainAddressResponse response = new MainAddressResponse();
        response.setAddressId(userAddress.getId());
        response.setCustomerName(userAddress.getName());
        response.setPhone(userAddress.getPhone());
        response.setAddressName(getAddressFormatted(userAddress.getCommuneId(), userAddress.getStreetAddress()));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UserAddressResponse> getCustomerAddresses(UserAddressRequest request) {
        User user = userDAO.findByUsername(request.getUsername()).get();
        List<UserAddress> userAddresses = userAddressDAO.findByUserIdAndDeletedAtIsNull(user.getId());

        List<UserAddressDTO> addresses = userAddresses.stream()
                .map(userAddress -> {
                    UserAddressDTO dto = new UserAddressDTO();
                    dto.setAddressId(userAddress.getId());
                    dto.setCustomerName(userAddress.getName());
                    dto.setPhone(userAddress.getPhone());
                    dto.setAddressName(getAddressFormatted(userAddress.getCommuneId(), userAddress.getStreetAddress()));
                    dto.setMainAddress(userAddress.isMainAddress());
                    return dto;
                }).collect(Collectors.toList());

        UserAddressResponse response = new UserAddressResponse();
        response.setAddresses(addresses);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public String getAddressFormatted(String communeId, String streetAddress) {
        Commune commune = communeDAO.findById(communeId).get();
        District district = districtDAO.findById(commune.getDistrictId()).get();
        City city = cityDAO.findById(district.getCityId()).get();
        return streetAddress + COMMA + commune.getName() + COMMA + district.getName() + COMMA + city.getName();
    }

    @Override
    public ResponseEntity<DeleteAddressResponse> deleteCustomerAddress(DeleteAddressRequest request) {
        User user = userDAO.findByUsername(request.getUsername()).get();
        Optional<UserAddress> optionalUserAddress = userAddressDAO
                .findUserAddressToDelete(user.getId(), request.getAddressId());

        optionalUserAddress.ifPresent(userAddress -> userAddress.setDeletedAt(LocalDateTime.now()));

        DeleteAddressResponse response = new DeleteAddressResponse();
        response.setMessage(DELETE_ADDRESS_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EditAddressResponse> editCustomerAddress(EditAddressRequest request) {
        Long addressId = request.getAddressId();
        User user = userDAO.findByUsername(request.getUsername()).get();
        Optional<UserAddress> optionalUserAddress = userAddressDAO
                .findUserAddressToEdit(user.getId(), addressId);
        Optional<Commune> optionalCommune = communeDAO.findById(request.getCommuneId());

        if (optionalUserAddress.isPresent() && optionalUserAddress.isPresent()) {
            UserAddress userAddress = optionalUserAddress.get();
            Commune commune = optionalCommune.get();

            userAddress.setName(request.getName());
            userAddress.setPhone(request.getPhone());
            userAddress.setCommuneId(commune.getId());
            userAddress.setStreetAddress(request.getStreetAddress());
        }

        EditAddressResponse response = new EditAddressResponse();
        response.setAddressId(addressId);
        response.setMessage(EDIT_ADDRESS_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CreateAddressResponse> createCustomerAddress(CreateAddressRequest request) {
        Optional<Commune> optionalCommune = communeDAO.findById(request.getCommuneId());
        if (optionalCommune.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_COMMUNE);
        }
        if (!InputValidation.isPhoneValid(request.getPhone())) {
            throw new GeneralException(HttpStatus.GONE, INVALID_PHONE_NUMBER);
        }
        User user = userDAO.findByUsername(request.getUsername()).get();

        Commune commune = optionalCommune.get();

        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(user.getId());
        userAddress.setMainAddress(false);
        userAddress.setStreetAddress(request.getStreetAddress());
        userAddress.setName(request.getFullName());
        userAddress.setPhone(request.getPhone());
        userAddress.setCommuneId(commune.getId());
        UserAddress savedUserAddress = userAddressDAO.save(userAddress);

        CreateAddressResponse response = new CreateAddressResponse();
        response.setAddressId(savedUserAddress.getId());
        response.setMessage(CREATE_ADDRESS_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CustomerProfileResponse> getCustomerProfile(CustomerProfileRequest request) {
        User user = userDAO.findByUsername(request.getUsername()).get();
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
        LocalDate dob;
        String email = request.getEmail();

        try {
            dob = DateFormatUtil.getLocalDate(request.getDateOfBirth(), DATE_PATTERN);
        } catch (DateTimeParseException e) {
            throw new GeneralException(HttpStatus.GONE, WRONG_LOCAL_DATE_FORMAT);
        }

        if (!InputValidation.isEmailValid(email)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_EMAIL);
        }

        User user = userDAO.findByUsername(request.getUsername()).get();
        user.setFullName(request.getFullName());
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
            IRepairerProfile repairerProfile = commentDAO.findRepairerProfile(repairerId);
            ISuccessfulRepair successfulRepair = commentDAO.findSuccessfulRepair(repairerId);
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

            List<IRepairerComment> repairComments = commentDAO.findRepairComments(repairerId, limit, offset);
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
}
