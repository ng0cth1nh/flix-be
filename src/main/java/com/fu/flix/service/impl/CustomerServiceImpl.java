package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
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
                               AppConf appConf) {
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

        if (!isMatchPaymentMethod(usingVoucherDTO, voucher)) {
            throw new GeneralException(PAYMENT_METHOD_NOT_VALID_FOR_THIS_VOUCHER);
        }

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

    private boolean isMatchPaymentMethod(UsingVoucherDTO usingVoucherDTO, Voucher voucher) {
        return voucher.getVoucherPaymentMethods().stream()
                .anyMatch(V -> V.getPaymentMethod().getId().equals(usingVoucherDTO.getPaymentMethodId()));
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
        if (!isCancelable(repairRequest)) {
            throw new GeneralException(ONLY_CAN_CANCEL_REQUEST_PENDING_OR_CONFIRMED);
        }

        updateUsedVoucherQuantity(repairRequest);

        repairRequest.setStatusId(CANCELLED.getId());

        CancelRequestingRepairResponse response = new CancelRequestingRepairResponse();
        response.setMessage(CANCEL_REPAIR_REQUEST_SUCCESSFUL);

        return new ResponseEntity<>(response, HttpStatus.OK);
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
            throw new GeneralException(INVALID_STATUS);
        }
    }

    @Override
    public ResponseEntity<DetailRequestingRepairResponse> getDetailFixingRequest(DetailRequestingRepairRequest request) {
        RepairRequest repairRequest = getRepairRequestValidated(request.getRequestCode(), request.getUsername());
        com.fu.flix.entity.Service service = serviceDAO.findById(repairRequest.getServiceId()).get();

        DetailRequestingRepairResponse response = new DetailRequestingRepairResponse();
        response.setServiceId(service.getId());
        response.setServiceName(service.getName());
        response.setAddressId(repairRequest.getAddressId());
        response.setExpectFixingDay(DateFormatUtil.toString(repairRequest.getExpectStartFixingAt(), DATE_TIME_PATTERN));
        response.setDescription(repairRequest.getDescription());
        response.setVoucherId(repairRequest.getVoucherId());
        response.setPaymentMethodId(repairRequest.getPaymentMethodId());
        response.setDate(DateFormatUtil.toString(repairRequest.getCreatedAt(), DATE_TIME_PATTERN));
        response.setPrice(getRepairRequestPrice(repairRequest, service.getInspectionPrice()));

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

        return repairRequest;
    }

    private Double getRepairRequestPrice(RepairRequest repairRequest, Double inspectionPrice) {
        Optional<Invoice> optionalInvoice = invoiceDAO.findByRequestCode(repairRequest.getRequestCode());
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
            throw new GeneralException(INVALID_COMMUNE);
        }
        if (!InputValidation.isPhoneValid(request.getPhone())) {
            throw new GeneralException(INVALID_PHONE_NUMBER);
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
            throw new GeneralException(WRONG_LOCAL_DATE_FORMAT);
        }

        if (!InputValidation.isEmailValid(email)) {
            throw new GeneralException(INVALID_EMAIL);
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
