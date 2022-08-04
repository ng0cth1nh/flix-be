package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.enums.*;
import com.fu.flix.dao.*;
import com.fu.flix.dto.*;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.entity.*;
import com.fu.flix.service.*;
import com.fu.flix.util.DateFormatUtil;
import com.fu.flix.util.InputValidation;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.fu.flix.constant.Constant.*;
import static com.fu.flix.constant.enums.CVStatus.ACCEPTED;
import static com.fu.flix.constant.enums.ServiceState.INACTIVE;
import static com.fu.flix.constant.enums.TransactionStatus.FAIL;
import static com.fu.flix.constant.enums.AccountState.ACTIVE;
import static com.fu.flix.constant.enums.TransactionStatus.PENDING;
import static com.fu.flix.constant.enums.TransactionType.WITHDRAW;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {
    private final ValidatorService validatorService;
    private final CategoryDAO categoryDAO;
    private final ImageDAO imageDAO;
    private final CloudStorageService cloudStorageService;
    private final AppConf appConf;
    private final ServiceDAO serviceDAO;
    private final CategoryService categoryService;
    private final SubServiceDAO subServiceDAO;
    private final RepairRequestDAO repairRequestDAO;
    private final UserDAO userDAO;
    private final FeedbackDAO feedbackDAO;
    private final RoleDAO roleDAO;
    private final AddressService addressService;
    private final FeedbackService feedbackService;
    private final FCMService fcmService;
    private final StatusDAO statusDAO;
    private final CertificateDAO certificateDAO;
    private final AccessoryDAO accessoryDAO;
    private final RepairerDAO repairerDAO;
    private final ExtraServiceDAO extraServiceDAO;
    private final TransactionHistoryDAO transactionHistoryDAO;
    private final VoucherService voucherService;
    private final InvoiceDAO invoiceDAO;
    private final BalanceDAO balanceDAO;
    private final Long NAME_MAX_LENGTH;
    private final Long DESCRIPTION_MAX_LENGTH;
    private final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final String DATE_PATTERN = "dd-MM-yyyy";

    public AdminServiceImpl(ValidatorService validatorService,
                            CategoryDAO categoryDAO,
                            ImageDAO imageDAO,
                            CloudStorageService cloudStorageService,
                            AppConf appConf,
                            ServiceDAO serviceDAO,
                            CategoryService categoryService,
                            SubServiceDAO subServiceDAO,
                            RepairRequestDAO repairRequestDAO,
                            UserDAO userDAO,
                            FeedbackDAO feedbackDAO,
                            RoleDAO roleDAO, AddressService addressService,
                            FeedbackService feedbackService,
                            FCMService fcmService, StatusDAO statusDAO,
                            CertificateDAO certificateDAO,
                            AccessoryDAO accessoryDAO,
                            RepairerDAO repairerDAO,
                            ExtraServiceDAO extraServiceDAO,
                            TransactionHistoryDAO transactionHistoryDAO,
                            VoucherService voucherService,
                            InvoiceDAO invoiceDAO,
                            BalanceDAO balanceDAO) {
        this.validatorService = validatorService;
        this.categoryDAO = categoryDAO;
        this.imageDAO = imageDAO;
        this.cloudStorageService = cloudStorageService;
        this.appConf = appConf;
        this.NAME_MAX_LENGTH = appConf.getNameMaxLength();
        this.DESCRIPTION_MAX_LENGTH = appConf.getDescriptionMaxLength();
        this.serviceDAO = serviceDAO;
        this.categoryService = categoryService;
        this.subServiceDAO = subServiceDAO;
        this.repairRequestDAO = repairRequestDAO;
        this.userDAO = userDAO;
        this.feedbackDAO = feedbackDAO;
        this.roleDAO = roleDAO;
        this.addressService = addressService;
        this.feedbackService = feedbackService;
        this.fcmService = fcmService;
        this.statusDAO = statusDAO;
        this.certificateDAO = certificateDAO;
        this.accessoryDAO = accessoryDAO;
        this.repairerDAO = repairerDAO;
        this.extraServiceDAO = extraServiceDAO;
        this.transactionHistoryDAO = transactionHistoryDAO;
        this.voucherService = voucherService;
        this.invoiceDAO = invoiceDAO;
        this.balanceDAO = balanceDAO;
    }

    @Override
    public ResponseEntity<GetAdminProfileResponse> getAdminProfile(GetAdminProfileRequest request) {
        User admin = validatorService.getUserValidated(request.getUserId());
        GetAdminProfileResponse response = new GetAdminProfileResponse();
        response.setFullName(admin.getFullName());
        response.setPhone(admin.getPhone());
        response.setEmail(admin.getEmail());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UpdateAdminProfileResponse> updateAdminProfile(UpdateAdminProfileRequest request) {
        String email = request.getEmail();
        if (!InputValidation.isEmailValid(email, true)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_EMAIL);
        }

        String fullName = request.getFullName();
        if (!InputValidation.isNameValid(fullName, NAME_MAX_LENGTH)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_FULL_NAME);
        }

        User admin = validatorService.getUserValidated(request.getUserId());
        admin.setFullName(fullName);
        admin.setEmail(email);

        UpdateAdminProfileResponse response = new UpdateAdminProfileResponse();
        response.setMessage(UPDATE_ADMIN_PROFILE_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GetCategoriesResponse> getCategories(GetCategoriesRequest request) {
        int pageSize = validatorService.getPageSize(request.getPageSize());
        int pageNumber = validatorService.getPageNumber(request.getPageNumber());

        Page<Category> categoryPage = categoryDAO.findAll(PageRequest.of(pageNumber, pageSize));
        long totalRecord = categoryDAO.count();

        List<CategoryDTO> categories = categoryPage.stream()
                .map(category -> {
                    Optional<Image> optionalIcon = imageDAO.findById(category.getIconId());
                    Optional<Image> optionalImage = imageDAO.findById(category.getImageId());

                    CategoryDTO dto = new CategoryDTO();
                    dto.setCategoryName(category.getName());
                    dto.setStatus(category.isActive() ? ServiceState.ACTIVE.name() : INACTIVE.name());
                    dto.setId(category.getId());
                    dto.setIcon(optionalIcon.map(Image::getUrl).orElse(null));
                    dto.setImage(optionalImage.map(Image::getUrl).orElse(null));
                    dto.setDescription(category.getDescription());
                    return dto;
                }).collect(Collectors.toList());

        GetCategoriesResponse response = new GetCategoriesResponse();
        response.setCategories(categories);
        response.setTotalRecord(totalRecord);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CreateCategoryResponse> createCategory(CreateCategoryRequest request) throws IOException {
        validateModifyCategory(request);

        boolean isActive = request.getIsActive() != null
                ? request.getIsActive()
                : true;

        Category category = new Category();
        category.setName(request.getCategoryName());
        category.setDescription(request.getDescription());
        category.setActive(isActive);
        postCategoryIcon(category, request.getIcon());
        postCategoryImage(category, request.getImage());
        categoryDAO.save(category);

        CreateCategoryResponse response = new CreateCategoryResponse();
        response.setMessage(CREATE_CATEGORY_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UpdateCategoryResponse> updateCategory(UpdateCategoryRequest request) throws IOException {
        validateModifyCategory(request);

        boolean isActive = request.getIsActive() != null
                ? request.getIsActive()
                : true;

        MultipartFile icon = request.getIcon();
        MultipartFile image = request.getImage();

        Category category = validatorService.getCategoryValidated(request.getId());
        category.setName(request.getCategoryName());
        category.setDescription(request.getDescription());
        category.setActive(isActive);
        if (icon != null) {
            postCategoryIcon(category, icon);
        }
        if (image != null) {
            postCategoryImage(category, image);
        }
        categoryDAO.save(category);

        UpdateCategoryResponse response = new UpdateCategoryResponse();
        response.setMessage(UPDATE_CATEGORY_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void validateModifyCategory(ModifyCategoryRequest request) {
        String description = request.getDescription();
        if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new GeneralException(HttpStatus.GONE, EXCEEDED_DESCRIPTION_LENGTH_ALLOWED);
        }

        String categoryName = request.getCategoryName();
        if (Strings.isEmpty(categoryName) || categoryName.length() > NAME_MAX_LENGTH) {
            throw new GeneralException(HttpStatus.GONE, INVALID_CATEGORY_NAME);
        }
    }

    private void postCategoryIcon(Category category, MultipartFile icon) throws IOException {
        String url = icon != null
                ? cloudStorageService.uploadImage(icon)
                : appConf.getDefaultIcon();
        Image savedImage = saveImage(category.getName(), url);
        category.setIconId(savedImage.getId());
    }

    private void postCategoryImage(Category category, MultipartFile image) throws IOException {
        String url = image != null
                ? cloudStorageService.uploadImage(image)
                : appConf.getDefaultImage();
        Image savedImage = saveImage(category.getName(), url);
        category.setImageId(savedImage.getId());
    }

    @Override
    public ResponseEntity<GetServicesResponse> getServices(GetServicesRequest request) {
        int pageSize = validatorService.getPageSize(request.getPageSize());
        int pageNumber = validatorService.getPageNumber(request.getPageNumber());

        Page<com.fu.flix.entity.Service> servicePage = serviceDAO.findAll(PageRequest.of(pageNumber, pageSize));
        long totalRecord = serviceDAO.count();

        List<ServiceDTO> serviceDTOS = servicePage.stream()
                .map(categoryService::mapToServiceDTO)
                .collect(Collectors.toList());

        GetServicesResponse response = new GetServicesResponse();
        response.setServices(serviceDTOS);
        response.setTotalRecord(totalRecord);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CreateServiceResponse> createService(CreateServiceRequest request) throws IOException {
        validateModifyService(request);

        boolean isActive = request.getIsActive() != null
                ? request.getIsActive()
                : true;

        com.fu.flix.entity.Service service = new com.fu.flix.entity.Service();
        service.setName(request.getServiceName());
        service.setInspectionPrice(request.getInspectionPrice());
        service.setDescription(request.getDescription());
        service.setCategoryId(request.getCategoryId());
        service.setActive(isActive);
        postServiceIcon(service, request.getIcon());
        postServiceImage(service, request.getImage());
        serviceDAO.save(service);

        CreateServiceResponse response = new CreateServiceResponse();
        response.setMessage(CREATE_SERVICE_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UpdateServiceResponse> updateService(UpdateServiceRequest request) throws IOException {
        validateModifyService(request);

        boolean isActive = request.getIsActive() != null
                ? request.getIsActive()
                : true;

        MultipartFile icon = request.getIcon();
        MultipartFile image = request.getImage();

        com.fu.flix.entity.Service service = validatorService.getServiceValidated(request.getServiceId());
        service.setName(request.getServiceName());
        service.setInspectionPrice(request.getInspectionPrice());
        service.setDescription(request.getDescription());
        service.setCategoryId(request.getCategoryId());
        service.setActive(isActive);
        if (icon != null) {
            postServiceIcon(service, icon);
        }
        if (image != null) {
            postServiceImage(service, image);
        }
        serviceDAO.save(service);

        UpdateServiceResponse response = new UpdateServiceResponse();
        response.setMessage(UPDATE_SERVICE_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void validateModifyService(ModifyServiceRequest request) {
        String description = request.getDescription();
        if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new GeneralException(HttpStatus.GONE, EXCEEDED_DESCRIPTION_LENGTH_ALLOWED);
        }

        String serviceName = request.getServiceName();
        if (Strings.isEmpty(serviceName) || serviceName.length() > NAME_MAX_LENGTH) {
            throw new GeneralException(HttpStatus.GONE, INVALID_SERVICE_NAME);
        }

        Long inspectionPrice = request.getInspectionPrice();
        if (inspectionPrice == null || inspectionPrice < 0) {
            throw new GeneralException(HttpStatus.GONE, INVALID_INSPECTION_PRICE);
        }

        Long categoryId = request.getCategoryId();
        if (categoryId == null) {
            throw new GeneralException(HttpStatus.GONE, CATEGORY_ID_IS_REQUIRED);
        }

        if (categoryDAO.findById(categoryId).isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, CATEGORY_NOT_FOUND);
        }
    }

    private void postServiceIcon(com.fu.flix.entity.Service service, MultipartFile icon) throws IOException {
        String url = icon != null
                ? cloudStorageService.uploadImage(icon)
                : appConf.getDefaultIcon();
        Image savedImage = saveImage(service.getName(), url);
        service.setIconId(savedImage.getId());
    }

    private void postServiceImage(com.fu.flix.entity.Service service, MultipartFile image) throws IOException {
        String url = image != null
                ? cloudStorageService.uploadImage(image)
                : appConf.getDefaultImage();
        Image savedImage = saveImage(service.getName(), url);
        service.setImageId(savedImage.getId());
    }

    private Image saveImage(String name, String url) {
        Image image = new Image();
        image.setName(name);
        image.setUrl(url);
        return imageDAO.save(image);
    }

    @Override
    public ResponseEntity<AdminSearchServicesResponse> searchServices(AdminSearchServicesRequest request) {
        String keyword = request.getKeyword();
        if (keyword == null || keyword.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_KEY_WORD);
        }

        List<IAdminSearchServiceDTO> services = serviceDAO.searchServicesForAdmin(keyword);
        List<AdminSearchServiceDTO> searchServiceDTOS = services.stream()
                .map(service -> {
                    AdminSearchServiceDTO dto = new AdminSearchServiceDTO();
                    dto.setServiceId(service.getServiceId());
                    dto.setServiceName(service.getServiceName());
                    dto.setIcon(service.getIcon());
                    dto.setImage(service.getImage());
                    dto.setStatus(service.getStatus());
                    dto.setDescription(service.getDescription());
                    return dto;
                }).collect(Collectors.toList());

        AdminSearchServicesResponse response = new AdminSearchServicesResponse();
        response.setServices(searchServiceDTOS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GetSubServicesResponse> getSubServices(GetSubServicesRequest request) {
        int pageSize = validatorService.getPageSize(request.getPageSize());
        int pageNumber = validatorService.getPageNumber(request.getPageNumber());

        Page<SubService> subServicePage = subServiceDAO.findAll(PageRequest.of(pageNumber, pageSize));
        long totalRecord = subServiceDAO.count();

        List<AdminSubServiceDTO> subServices = subServicePage.stream()
                .map(subService -> {
                    AdminSubServiceDTO dto = new AdminSubServiceDTO();
                    dto.setId(subService.getId());
                    dto.setSubServiceName(subService.getName());
                    dto.setPrice(subService.getPrice());
                    dto.setStatus(subService.getIsActive() ? ServiceState.ACTIVE.name() : INACTIVE.name());
                    dto.setDescription(subService.getDescription());
                    return dto;
                }).collect(Collectors.toList());

        GetSubServicesResponse response = new GetSubServicesResponse();
        response.setSubServices(subServices);
        response.setTotalRecord(totalRecord);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CreateSubServiceResponse> createSubService(CreateSubServiceRequest request) {
        validateModifySubService(request);

        boolean isActive = request.getIsActive() != null
                ? request.getIsActive()
                : true;

        SubService subService = new SubService();
        subService.setName(request.getSubServiceName());
        subService.setDescription(request.getDescription());
        subService.setPrice(request.getPrice());
        subService.setServiceId(request.getServiceId());
        subService.setIsActive(isActive);
        subServiceDAO.save(subService);

        CreateSubServiceResponse response = new CreateSubServiceResponse();
        response.setMessage(CREATE_SUB_SERVICE_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UpdateSubServiceResponse> updateSubService(UpdateSubServiceRequest request) {
        validateModifySubService(request);

        boolean isActive = request.getIsActive() != null
                ? request.getIsActive()
                : true;

        SubService subService = validatorService.getSubServiceValidated(request.getSubServiceId());
        subService.setName(request.getSubServiceName());
        subService.setDescription(request.getDescription());
        subService.setPrice(request.getPrice());
        subService.setServiceId(request.getServiceId());
        subService.setIsActive(isActive);
        subServiceDAO.save(subService);

        UpdateSubServiceResponse response = new UpdateSubServiceResponse();
        response.setMessage(UPDATE_SUB_SERVICE_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void validateModifySubService(ModifySubServiceRequest request) {
        String subServiceName = request.getSubServiceName();
        if (Strings.isEmpty(subServiceName) || subServiceName.length() > NAME_MAX_LENGTH) {
            throw new GeneralException(HttpStatus.GONE, INVALID_SUB_SERVICE_NAME);
        }

        Long price = request.getPrice();
        if (price == null || price < 0) {
            throw new GeneralException(HttpStatus.GONE, INVALID_PRICE);
        }

        String description = request.getDescription();
        if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new GeneralException(HttpStatus.GONE, EXCEEDED_DESCRIPTION_LENGTH_ALLOWED);
        }

        validatorService.getServiceValidated(request.getServiceId());
    }

    @Override
    public ResponseEntity<AdminRequestingResponse> getRequests(AdminRequestingRequest request) {
        int pageNumber = validatorService.getPageNumber(request.getPageNumber());
        int pageSize = validatorService.getPageSize(request.getPageSize());

        int end = (pageNumber + 1) * pageSize;
        int start = end - (pageSize - 1);

        List<IRequestInfoDTO> requestDTOs = repairRequestDAO.findAllRequestForAdmin(start, end);
        long totalRecord = repairRequestDAO.count();

        List<AdminRequestingDTO> requestList = requestDTOs.stream()
                .map(rq -> {
                    AdminRequestingDTO dto = new AdminRequestingDTO();
                    dto.setRequestCode(rq.getRequestCode());
                    dto.setCustomerId(rq.getCustomerId());
                    dto.setCustomerName(rq.getCustomerName());
                    dto.setCustomerPhone(rq.getCustomerPhone());
                    dto.setRepairerId(rq.getRepairerId());
                    dto.setRepairerName(rq.getRepairerName());
                    dto.setRepairerPhone(rq.getRepairerPhone());
                    dto.setStatus(rq.getStatus());
                    dto.setCreatedAt(DateFormatUtil.toString(rq.getCreatedAt(), DATE_TIME_PATTERN));
                    return dto;
                }).collect(Collectors.toList());

        AdminRequestingResponse response = new AdminRequestingResponse();
        response.setRequestList(requestList);
        response.setTotalRecord(totalRecord);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GetCustomersResponse> getCustomers(GetCustomersRequest request) {
        int pageNumber = validatorService.getPageNumber(request.getPageNumber());
        int pageSize = validatorService.getPageSize(request.getPageSize());

        int offset = pageNumber * pageSize;
        List<ICustomerDTO> customerDTOs = userDAO.findCustomersForAdmin(pageSize, offset);
        long totalRecord = userDAO.countCustomers();

        GetCustomersResponse response = new GetCustomersResponse();
        response.setCustomerList(customerDTOs);
        response.setTotalRecord(totalRecord);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GetRepairersResponse> getRepairers(GetRepairersRequest request) {
        int pageNumber = validatorService.getPageNumber(request.getPageNumber());
        int pageSize = validatorService.getPageSize(request.getPageSize());

        int offset = pageNumber * pageSize;
        List<IRepairerDTO> repairerDTOS = userDAO.findRepairersForAdmin(pageSize, offset);
        long totalRecord = userDAO.countRepairers();

        GetRepairersResponse response = new GetRepairersResponse();
        response.setRepairerList(repairerDTOS);
        response.setTotalRecord(totalRecord);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GetCustomerDetailResponse> getCustomerDetail(GetCustomerDetailRequest request) {
        Optional<ICustomerDetailDTO> optionalCustomer = userDAO.findCustomerDetail(request.getCustomerId());
        if (optionalCustomer.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, CUSTOMER_NOT_FOUND);
        }

        ICustomerDetailDTO dto = optionalCustomer.get();
        String dob = dto.getDateOfBirth() != null
                ? DateFormatUtil.toString(dto.getDateOfBirth(), DATE_PATTERN)
                : null;

        GetCustomerDetailResponse response = new GetCustomerDetailResponse();
        response.setAvatar(dto.getAvatar());
        response.setCustomerName(dto.getCustomerName());
        response.setCustomerPhone(dto.getCustomerPhone());
        response.setDateOfBirth(dob);
        response.setStatus(dto.getStatus());
        response.setGender(dto.getGender());
        response.setEmail(dto.getEmail());
        response.setAddress(addressService.getAddressFormatted(dto.getAddressId()));
        response.setCreatedAt(DateFormatUtil.toString(dto.getCreatedAt(), DATE_TIME_PATTERN));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GetBanUsersResponse> getBanUsers(GetBanUsersRequest request) {
        int pageNumber = validatorService.getPageNumber(request.getPageNumber());
        int pageSize = validatorService.getPageSize(request.getPageSize());

        int offset = pageNumber * pageSize;

        List<IBanUserDTO> banUserDTOs = userDAO.findBanUsers(pageSize, offset);
        long totalRecord = userDAO.countBanUsers();

        List<BanUserDTO> userList = banUserDTOs.stream()
                .map(banUser -> {
                    BanUserDTO dto = new BanUserDTO();
                    dto.setId(banUser.getId());
                    dto.setAvatar(banUser.getAvatar());
                    dto.setName(banUser.getName());
                    dto.setPhone(banUser.getPhone());
                    dto.setRole(banUser.getRole());
                    dto.setBanReason(banUser.getBanReason());
                    dto.setBanAt(DateFormatUtil.toString(banUser.getBanAt(), DATE_TIME_PATTERN));
                    return dto;
                }).collect(Collectors.toList());

        GetBanUsersResponse response = new GetBanUsersResponse();
        response.setUserList(userList);
        response.setTotalRecord(totalRecord);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<BanUserResponse> banUser(BanUserRequest request) {
        String phone = request.getPhone();
        if (!InputValidation.isPhoneValid(phone)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_PHONE_NUMBER);
        }

        String banReason = request.getBanReason();
        if (Strings.isEmpty(banReason) || banReason.length() > DESCRIPTION_MAX_LENGTH) {
            throw new GeneralException(HttpStatus.GONE, INVALID_BAN_REASON);
        }

        Optional<User> optionalUser = userDAO.findByUsername(phone);
        if (optionalUser.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, USER_NOT_FOUND);
        }

        User user = optionalUser.get();
        if (!user.getIsActive()) {
            throw new GeneralException(HttpStatus.GONE, THIS_ACCOUNT_HAS_BEEN_BANNED);
        }

        if (!isUser(user.getRoles())) {
            throw new GeneralException(HttpStatus.GONE, JUST_CAN_BAN_USER_ROLE_ARE_CUSTOMER_OR_REPAIRER_OR_PENDING_REPAIRER);
        }

        user.setIsActive(false);
        user.setBanReason(banReason);
        user.setBanAt(LocalDateTime.now());

        BanUserResponse response = new BanUserResponse();
        response.setMessage(BAN_USER_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private boolean isUser(Collection<Role> roles) {
        for (Role r : roles) {
            if (isUser(r)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUser(Role r) {
        String roleName = r.getName();
        return RoleType.ROLE_CUSTOMER.name().equals(roleName)
                || RoleType.ROLE_REPAIRER.name().equals(roleName)
                || RoleType.ROLE_PENDING_REPAIRER.name().equals(roleName);
    }

    @Override
    public ResponseEntity<AdminCreateFeedBackResponse> createFeedback(AdminCreateFeedBackRequest request) throws IOException {
        validatorService.validateCreateFeedbackInput(request);

        String phone = request.getPhone();
        if (!InputValidation.isPhoneValid(phone)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_PHONE_NUMBER);
        }

        Optional<User> optionalUser = userDAO.findByUsername(phone);
        if (optionalUser.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, USER_NOT_FOUND);
        }

        User user = optionalUser.get();
        Feedback feedback = new Feedback();
        feedback.setCreatedById(request.getUserId());
        feedback.setUserId(user.getId());
        feedback.setTitle(request.getTitle());
        feedback.setDescription(request.getDescription());
        feedback.setStatusId(FeedbackStatus.PENDING.getId());
        feedback.setType(request.getFeedbackType());
        feedback.setRequestCode(request.getRequestCode());
        feedbackService.postFeedbackImages(feedback, request.getImages());
        feedbackDAO.save(feedback);

        AdminCreateFeedBackResponse response = new AdminCreateFeedBackResponse();
        response.setMessage(CREATE_FEEDBACK_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<FeedbackDetailResponse> getFeedbackDetail(FeedbackDetailRequest request) {
        Feedback feedback = validatorService.getFeedbackValidated(request.getFeedbackId());
        List<String> images = feedback.getImages()
                .stream().map(Image::getUrl)
                .collect(Collectors.toList());
        Optional<User> optionalUser = userDAO.findById(feedback.getUserId());
        Optional<Status> optionalStatus = statusDAO.findById(feedback.getStatusId());

        FeedbackDetailResponse response = new FeedbackDetailResponse();
        response.setPhone(optionalUser.map(User::getPhone).orElse(null));
        response.setFeedbackType(feedback.getType());
        response.setRequestCode(feedback.getRequestCode());
        response.setTitle(feedback.getTitle());
        response.setDescription(feedback.getDescription());
        response.setImages(images);
        response.setStatus(optionalStatus.map(Status::getName).orElse(null));
        response.setResponse(feedback.getResponse());
        response.setHandleByAdminId(feedback.getHandleByAdminId());
        response.setCreatedById(feedback.getCreatedById());
        response.setUserId(feedback.getUserId());
        response.setCreatedAt(DateFormatUtil.toString(feedback.getCreatedAt(), DATE_TIME_PATTERN));
        response.setUpdatedAt(DateFormatUtil.toString(feedback.getUpdatedAt(), DATE_TIME_PATTERN));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AdminGetAccessoriesResponse> getAccessories(AdminGetAccessoriesRequest request) {
        int pageSize = validatorService.getPageSize(request.getPageSize());
        int pageNumber = validatorService.getPageNumber(request.getPageNumber());

        Page<Accessory> accessoryPage = accessoryDAO.findAll(PageRequest.of(pageNumber, pageSize));
        long totalRecord = accessoryDAO.count();

        List<AccessoryOutputDTO> accessoryList = accessoryPage.stream()
                .map(accessory -> {
                    AccessoryOutputDTO dto = new AccessoryOutputDTO();
                    dto.setId(accessory.getId());
                    dto.setName(accessory.getName());
                    dto.setPrice(accessory.getPrice());
                    dto.setInsuranceTime(accessory.getInsuranceTime());
                    dto.setManufacture(accessory.getManufacture());
                    dto.setCountry(accessory.getCountry());
                    dto.setDescription(accessory.getDescription());
                    return dto;
                }).collect(Collectors.toList());

        AdminGetAccessoriesResponse response = new AdminGetAccessoriesResponse();
        response.setAccessoryList(accessoryList);
        response.setTotalRecord(totalRecord);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PendingRepairersResponse> getPendingRepairers(PendingRepairersRequest request) {
        int pageNumber = validatorService.getPageNumber(request.getPageNumber());
        int pageSize = validatorService.getPageSize(request.getPageSize());

        int offset = pageNumber * pageSize;
        List<IPendingRepairerDTO> pendingRepairerDTOs = userDAO.findPendingRepairers(pageSize, offset);
        long totalRecord = userDAO.countPendingRepairers();

        List<PendingRepairerDTO> repairerList = pendingRepairerDTOs.stream()
                .map(pr -> {
                    PendingRepairerDTO dto = new PendingRepairerDTO();
                    dto.setId(pr.getId());
                    dto.setRepairerName(pr.getRepairerName());
                    dto.setRepairerPhone(pr.getRepairerPhone());
                    dto.setCreatedAt(DateFormatUtil.toString(pr.getCreatedAt(), DATE_TIME_PATTERN));
                    return dto;
                }).collect(Collectors.toList());

        PendingRepairersResponse response = new PendingRepairersResponse();
        response.setRepairerList(repairerList);
        response.setTotalRecord(totalRecord);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CreateAccessoryResponse> createAccessory(CreateAccessoryRequest request) {
        validateModifyAccessory(request);

        Accessory accessory = new Accessory();
        buildAccessory(request, accessory);

        accessoryDAO.save(accessory);

        CreateAccessoryResponse response = new CreateAccessoryResponse();
        response.setMessage(CREATE_ACCESSORY_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UpdateAccessoryResponse> updateAccessory(UpdateAccessoryRequest request) {
        validateModifyAccessory(request);

        Accessory accessory = validatorService.getAccessoryValidated(request.getId());
        buildAccessory(request, accessory);

        UpdateAccessoryResponse response = new UpdateAccessoryResponse();
        response.setMessage(UPDATE_ACCESSORY_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void validateModifyAccessory(ModifyAccessoryRequest request) {
        Long price = request.getPrice();
        if (price == null || price < 0) {
            throw new GeneralException(HttpStatus.GONE, INVALID_PRICE);
        }

        Integer insurance = request.getInsurance();
        if (insurance != null && insurance < 0) {
            throw new GeneralException(HttpStatus.GONE, INVALID_INSURANCE);
        }

        String description = request.getDescription();
        if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new GeneralException(HttpStatus.GONE, EXCEEDED_DESCRIPTION_LENGTH_ALLOWED);
        }

        String accessoryName = request.getAccessoryName();
        if (Strings.isEmpty(accessoryName) || accessoryName.length() > NAME_MAX_LENGTH) {
            throw new GeneralException(HttpStatus.GONE, INVALID_ACCESSORY_NAME);
        }

        Long serviceId = request.getServiceId();
        if (serviceId == null) {
            throw new GeneralException(HttpStatus.GONE, SERVICE_ID_IS_REQUIRED);
        }

        if (serviceDAO.findById(serviceId).isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_SERVICE);
        }
    }

    private void buildAccessory(ModifyAccessoryRequest request, Accessory accessory) {
        accessory.setName(request.getAccessoryName());
        accessory.setDescription(request.getDescription());
        accessory.setPrice(request.getPrice());
        accessory.setServiceId(request.getServiceId());
        accessory.setInsuranceTime(request.getInsurance());
        accessory.setCountry(request.getCountry());
        accessory.setManufacture(request.getManufacturer());
    }

    @Override
    public ResponseEntity<ResponseFeedbackResponse> responseFeedback(ResponseFeedbackRequest request) throws IOException {
        String status = getFeedbackStatusValidated(request.getStatus());
        String adminResponse = request.getResponse();
        if (Strings.isEmpty(adminResponse) || adminResponse.length() > DESCRIPTION_MAX_LENGTH) {
            throw new GeneralException(HttpStatus.GONE, INVALID_RESPONSE);
        }

        Feedback feedback = validatorService.getFeedbackValidated(request.getId());
        feedback.setStatusId(FeedbackStatus.valueOf(status).getId());
        feedback.setResponse(adminResponse);
        feedback.setHandleByAdminId(request.getUserId());

        fcmService.sendNotification("feedback", getFeedbackNotificationKey(status), feedback.getUserId(), request.getResponse());

        ResponseFeedbackResponse response = new ResponseFeedbackResponse();
        response.setMessage(RESPONSE_FEEDBACK_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String getFeedbackNotificationKey(String status) {
        if (FeedbackStatus.PENDING.name().equals(status)) return NotificationType.FEEDBACK_PENDING.name();
        else if (FeedbackStatus.PROCESSING.name().equals(status)) return NotificationType.FEEDBACK_PROCESSING.name();
        else if (FeedbackStatus.REJECTED.name().equals(status)) return NotificationType.FEEDBACK_REJECTED.name();
        else return NotificationType.FEEDBACK_DONE.name();
    }

    private String getFeedbackStatusValidated(String status) {
        for (FeedbackStatus stt : FeedbackStatus.values()) {
            if (stt.name().equals(status)) {
                return status;
            }
        }
        throw new GeneralException(HttpStatus.GONE, INVALID_FEEDBACK_STATUS);
    }

    @Override
    public ResponseEntity<FeedbacksResponse> getFeedbacks(FeedbacksRequest request) {
        int pageSize = validatorService.getPageSize(request.getPageSize());
        int pageNumber = validatorService.getPageNumber(request.getPageNumber());

        List<Feedback> feedbackQueries = feedbackDAO.findAllByOrderByCreatedAtDesc(PageRequest.of(pageNumber, pageSize));
        long totalRecord = feedbackDAO.count();

        List<FeedbackDTO> feedbackList = feedbackQueries.stream()
                .map(feedback -> {
                    Optional<User> optionalUser = userDAO.findById(feedback.getUserId());
                    Optional<Status> optionalStatus = statusDAO.findById(feedback.getStatusId());

                    FeedbackDTO dto = new FeedbackDTO();
                    dto.setId(feedback.getId());
                    dto.setPhone(optionalUser.map(User::getPhone).orElse(null));
                    dto.setFeedbackType(feedback.getType());
                    dto.setTitle(feedback.getTitle());
                    dto.setCreatedAt(DateFormatUtil.toString(feedback.getCreatedAt(), DATE_TIME_PATTERN));
                    dto.setStatus(optionalStatus.map(Status::getName).orElse(null));
                    return dto;
                }).collect(Collectors.toList());

        FeedbacksResponse response = new FeedbacksResponse();
        response.setFeedbackList(feedbackList);
        response.setTotalRecord(totalRecord);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AcceptCVResponse> acceptCV(AcceptCVRequest request) throws IOException {
        User user = validatorService.getUserValidated(request.getRepairerId());
        Collection<Role> roles = user.getRoles();
        if (!isPendingRepairer(roles)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_PENDING_REPAIRER);
        }

        Repairer repairer = repairerDAO.findByUserId(user.getId()).get();
        repairer.setAcceptedAccountAt(LocalDateTime.now());
        repairer.setCvStatus(ACCEPTED.name());
        updateToRepairerRole(roles);

        fcmService.sendNotification("register", NotificationType.REGISTER_SUCCESS.name(), user.getId());

        AcceptCVResponse response = new AcceptCVResponse();
        response.setMessage(ACCEPT_CV_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private boolean isPendingRepairer(Collection<Role> roles) {
        for (Role role : roles) {
            if (RoleType.ROLE_PENDING_REPAIRER.name().equals(role.getName())) {
                return true;
            }
        }
        return false;
    }

    private void updateToRepairerRole(Collection<Role> roles) {
        for (Role role : roles) {
            if (RoleType.ROLE_PENDING_REPAIRER.name().equals(role.getName())) {
                roles.remove(role);
                Role repairerRole = roleDAO.findByName(RoleType.ROLE_REPAIRER.name()).get();
                roles.add(repairerRole);
            }
        }
    }

    @Override
    public ResponseEntity<GetRepairerDetailResponse> getRepairerDetail(GetRepairerDetailRequest request) {
        Long repairerId = request.getRepairerId();
        Optional<IRepairerDetailDTO> optionalRepairer = userDAO.findRepairerDetail(repairerId);
        if (optionalRepairer.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, REPAIRER_NOT_FOUND);
        }

        List<Certificate> certificates = certificateDAO.findByRepairerId(repairerId);

        IRepairerDetailDTO dto = optionalRepairer.get();
        String dob = dto.getDateOfBirth() != null
                ? DateFormatUtil.toString(dto.getDateOfBirth(), DATE_PATTERN)
                : null;

        String acceptedAccountAt = dto.getAcceptedAccountAt() != null
                ? DateFormatUtil.toString(dto.getAcceptedAccountAt(), DATE_TIME_PATTERN)
                : null;

        List<IAdminCheckRegisterServiceDTO> registerServiceDTOs = repairerDAO.findRegisterServicesForAdmin(repairerId);

        GetRepairerDetailResponse response = new GetRepairerDetailResponse();
        response.setId(dto.getId());
        response.setAvatar(dto.getAvatar());
        response.setRepairerName(dto.getRepairerName());
        response.setRepairerPhone(dto.getRepairerPhone());
        response.setStatus(dto.getStatus());
        response.setDateOfBirth(dob);
        response.setGender(dto.getGender());
        response.setEmail(dto.getEmail());
        response.setAddress(addressService.getAddressFormatted(dto.getAddressId()));
        response.setCreatedAt(DateFormatUtil.toString(dto.getCreatedAt(), DATE_TIME_PATTERN));
        response.setExperienceYear(dto.getExperienceYear());
        response.setExperienceDescription(dto.getExperienceDescription());
        response.setIdentityCardNumber(dto.getIdentityCardNumber());
        response.setIdentityCardType(dto.getIdentityCardType());
        response.setFrontImage(dto.getFrontImage());
        response.setBackSideImage(dto.getBackSideImage());
        response.setAcceptedAccountAt(acceptedAccountAt);
        response.setCertificates(certificates.stream().map(Certificate::getUrl).collect(Collectors.toList()));
        response.setRole(dto.getRole());
        response.setRegisterServices(registerServiceDTOs);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SearchCategoriesResponse> searchCategories(SearchCategoriesRequest request) {
        String keyword = request.getKeyword();
        if (keyword == null || keyword.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_KEY_WORD);
        }

        List<ICategoryDTO> categories = categoryDAO.searchCategories(keyword);
        SearchCategoriesResponse response = new SearchCategoriesResponse();
        response.setCategories(categories);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SearchFeedbackResponse> searchFeedbacks(SearchFeedbackRequest request) {
        String phone = Strings.isEmpty(request.getKeyword())
                ? Strings.EMPTY
                : request.getKeyword();

        List<String> feedbackStatusIds = getQueryFeedbackStatusIds(request.getStatus());
        List<String> feedbackTypes = getQueryFeedbackTypes(request.getFeedbackType());

        List<ISearchFeedbackDTO> feedbacks = feedbackDAO
                .searchFeedbackForAdmin(phone, feedbackStatusIds, feedbackTypes);

        SearchFeedbackResponse response = new SearchFeedbackResponse();
        response.setFeedbackList(feedbacks);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private List<String> getQueryFeedbackStatusIds(String status) {
        if (Strings.isEmpty(status)) {
            return Arrays.stream(FeedbackStatus.values())
                    .map(FeedbackStatus::getId)
                    .collect(Collectors.toList());
        }

        List<String> result = new ArrayList<>();
        result.add(FeedbackStatus.valueOf(getFeedbackStatusValidated(status)).getId());
        return result;
    }


    private List<String> getQueryFeedbackTypes(String type) {
        if (Strings.isEmpty(type)) {
            return Arrays.stream(FeedbackType.values())
                    .map(FeedbackType::name)
                    .collect(Collectors.toList());
        }

        List<String> result = new ArrayList<>();
        result.add(getFeedbackTypeValidated(type));
        return result;
    }

    private String getFeedbackTypeValidated(String type) {
        for (FeedbackType ft : FeedbackType.values()) {
            if (ft.name().equals(type)) {
                return type;
            }
        }
        throw new GeneralException(HttpStatus.GONE, INVALID_FEEDBACK_TYPE);
    }

    @Override
    public ResponseEntity<SearchCustomersResponse> searchCustomers(SearchCustomersRequest request) {
        String phone = request.getKeyword();
        if (Strings.isEmpty(phone)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_KEY_WORD);
        }

        String accountState = getAccountStateValidated(request.getStatus());
        Boolean isActiveState = ACTIVE.name().equals(accountState);

        List<ISearchCustomerDTO> customers = userDAO.searchCustomersForAdmin(phone, isActiveState);
        SearchCustomersResponse response = new SearchCustomersResponse();
        response.setCustomers(customers);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SearchRepairersResponse> searchRepairers(SearchRepairersRequest request) {
        String phone = request.getKeyword();
        if (Strings.isEmpty(phone)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_KEY_WORD);
        }

        String accountState = getAccountStateValidated(request.getStatus());
        Boolean isActiveState = ACTIVE.name().equals(accountState);

        Boolean isVerified = request.getIsVerified();
        if (isVerified == null) {
            throw new GeneralException(HttpStatus.GONE, ACCOUNT_VERIFY_PARAM_IS_REQUIRED);
        }

        List<ISearchRepairersDTO> repairers = userDAO.searchRepairersForAdmin(phone, isActiveState, isVerified);
        SearchRepairersResponse response = new SearchRepairersResponse();
        response.setRepairers(repairers);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String getAccountStateValidated(String state) {
        for (AccountState us : AccountState.values()) {
            if (us.name().equals(state)) {
                return state;
            }
        }
        throw new GeneralException(HttpStatus.GONE, INVALID_STATUS);
    }

    @Override
    public ResponseEntity<AdminSearchAccessoriesResponse> searchAccessories(AdminSearchAccessoriesRequest request) {
        String keyword = request.getKeyword();
        if (Strings.isEmpty(keyword)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_KEY_WORD);
        }

        List<Accessory> accessories = accessoryDAO.searchAccessories(keyword);
        List<AccessoryOutputDTO> accessoryOutputDTOS = accessories.stream()
                .map(accessory -> {
                    AccessoryOutputDTO dto = new AccessoryOutputDTO();
                    dto.setId(accessory.getId());
                    dto.setName(accessory.getName());
                    dto.setPrice(accessory.getPrice());
                    dto.setInsuranceTime(accessory.getInsuranceTime());
                    dto.setManufacture(accessory.getManufacture());
                    dto.setCountry(accessory.getCountry());
                    return dto;
                }).collect(Collectors.toList());

        AdminSearchAccessoriesResponse response = new AdminSearchAccessoriesResponse();
        response.setAccessories(accessoryOutputDTOS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<TransactionsResponse> getTransactions(TransactionsRequest request) {
        int pageNumber = validatorService.getPageNumber(request.getPageNumber());
        int pageSize = validatorService.getPageSize(request.getPageSize());

        int offset = pageNumber * pageSize;

        List<ITransactionDTO> transactions = transactionHistoryDAO.findTransactionsForAdmin(pageSize, offset);
        long totalRecord = transactionHistoryDAO.count();

        TransactionsResponse response = new TransactionsResponse();
        response.setTransactions(transactions);
        response.setTotalRecord(totalRecord);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AdminGetRequestDetailResponse> getRequestDetail(AdminGetRequestDetailRequest request) {
        String requestCode = request.getRequestCode();
        if (Strings.isEmpty(requestCode)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_REQUEST_CODE);
        }

        AdminGetRequestDetailResponse response = new AdminGetRequestDetailResponse();
        Optional<IDetailRequestDTO> requestDetail = repairRequestDAO.findRequestDetailForAdmin(requestCode);
        requestDetail.ifPresent(rd -> {
            VoucherDTO voucherInfo = voucherService.getVoucherInfo(rd.getVoucherId());
            Invoice invoice = invoiceDAO.findByRequestCode(requestCode).get();

            response.setRequestCode(requestCode);
            response.setCustomerName(rd.getCustomerName());
            response.setCustomerPhone(rd.getCustomerPhone());
            response.setRepairerName(rd.getRepairerName());
            response.setRepairerPhone(rd.getRepairerPhone());
            response.setStatus(rd.getStatus());
            response.setCustomerAddress(addressService.getAddressFormatted(rd.getCustomerAddressId()));
            response.setDescription(rd.getDescription());
            response.setServiceName(rd.getServiceName());
            response.setVoucherCode(voucherInfo.getVoucherCode());
            response.setVoucherDiscount(voucherInfo.getVoucherDiscount());
            response.setVoucherDescription(voucherInfo.getVoucherDescription());
            response.setExpectedFixingTime(DateFormatUtil.toString(rd.getExpectedFixingTime(), DATE_TIME_PATTERN));
            response.setPaymentMethod(rd.getPaymentMethod());
            response.setCancelReason(rd.getCancelReason());
            response.setCreatedAt(DateFormatUtil.toString(rd.getCreatedAt(), DATE_TIME_PATTERN));
            response.setTotalPrice(rd.getTotalPrice());
            response.setVatPrice(rd.getVatPrice());
            response.setActualPrice(rd.getActualPrice());
            response.setTotalDiscount(rd.getTotalDiscount());
            response.setInspectionPrice(rd.getInspectionPrice());
            response.setTotalSubServicePrice(rd.getTotalSubServicePrice());
            response.setTotalAccessoryPrice(rd.getTotalAccessoryPrice());
            response.setTotalExtraServicePrice(rd.getTotalExtraServicePrice());
            response.setSubServices(getSubServiceDTOs(invoice));
            response.setAccessories(getAccessoryDTOs(invoice));
            response.setExtraServices(getExtraServiceDTOs(requestCode));
        });

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private List<SubServiceOutputDTO> getSubServiceDTOs(Invoice invoice) {
        return invoice.getSubServices().stream()
                .map(subService -> {
                    SubServiceOutputDTO dto = new SubServiceOutputDTO();
                    dto.setName(subService.getName());
                    dto.setPrice(subService.getPrice());
                    return dto;
                }).collect(Collectors.toList());
    }

    private List<AccessoryOutputDTO> getAccessoryDTOs(Invoice invoice) {
        return invoice.getAccessories().stream()
                .map(accessory -> {
                    AccessoryOutputDTO dto = new AccessoryOutputDTO();
                    dto.setName(accessory.getName());
                    dto.setPrice(accessory.getPrice());
                    return dto;
                }).collect(Collectors.toList());
    }

    private List<ExtraServiceOutputDTO> getExtraServiceDTOs(String requestCode) {
        return extraServiceDAO.findByRequestCode(requestCode).stream()
                .map(extraService -> {
                    ExtraServiceOutputDTO dto = new ExtraServiceOutputDTO();
                    dto.setName(extraService.getName());
                    dto.setPrice(extraService.getPrice());
                    return dto;
                }).collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<AdminSearchSubServicesResponse> searchSubServices(AdminSearchServicesRequest request) {
        String keyword = request.getKeyword();
        if (Strings.isEmpty(keyword)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_KEY_WORD);
        }

        List<SubService> subServiceDTOs = subServiceDAO.searchSubServicesForAdmin(keyword);
        List<SubServiceOutputDTO> subServices = subServiceDTOs.stream()
                .map(subService -> {
                    SubServiceOutputDTO dto = new SubServiceOutputDTO();
                    dto.setId(subService.getId());
                    dto.setName(subService.getName());
                    dto.setPrice(subService.getPrice());
                    dto.setStatus(subService.getIsActive() ? "ACTIVE" : "INACTIVE");
                    return dto;
                }).collect(Collectors.toList());

        AdminSearchSubServicesResponse response = new AdminSearchSubServicesResponse();
        response.setSubServices(subServices);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<TransactionDetailResponse> getTransactionDetail(TransactionDetailRequest request) {
        Long transactionId = request.getId();
        if (transactionId == null) {
            throw new GeneralException(HttpStatus.GONE, INVALID_TRANSACTION_ID);
        }

        TransactionDetailResponse response = new TransactionDetailResponse();
        Optional<ITransactionDetailDTO> optionalTransaction = transactionHistoryDAO.findTransactionDetail(transactionId);
        optionalTransaction.ifPresent(transaction -> {
            response.setId(transaction.getId());
            response.setTransactionCode(transaction.getTransactionCode());
            response.setVnpTransactionNo(transaction.getVnpTransactionNo());
            response.setAmount(transaction.getAmount());
            response.setTransactionType(transaction.getTransactionType());
            response.setFullName(transaction.getFullName());
            response.setPhone(transaction.getPhone());
            response.setPayDate(transaction.getPayDate());
            response.setBankCode(transaction.getBankCode());
            response.setCardType(transaction.getCardType());
            response.setOrderInfo(transaction.getOrderInfo());
            response.setVnpBankTranNo(transaction.getVnpBankTranNo());
            response.setStatus(transaction.getStatus());
            response.setFailReason(transaction.getFailReason());
        });

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SearchTransactionsResponse> searchTransactions(SearchTransactionsRequest request) {
        String requestCode = request.getKeyword() == null
                ? Strings.EMPTY
                : request.getKeyword();

        List<String> transactionTypes = getTransactionTypeQueries(request.getTransactionType());
        List<String> transactionStatus = getTransactionStatusQueries(request.getStatus());

        List<ITransactionDTO> transactions = transactionHistoryDAO
                .searchTransactionsForAdmin(requestCode, transactionTypes, transactionStatus);

        SearchTransactionsResponse response = new SearchTransactionsResponse();
        response.setTransactions(transactions);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private List<String> getTransactionTypeQueries(String transactionType) {
        if (Strings.isEmpty(transactionType)) {
            return Arrays.stream(TransactionType.values())
                    .map(Enum::name)
                    .collect(Collectors.toList());
        }

        List<String> result = new ArrayList<>();
        result.add(getTransactionTypeValidated(transactionType));
        return result;
    }

    private String getTransactionTypeValidated(String transactionType) {
        for (TransactionType tt : TransactionType.values()) {
            if (tt.name().equals(transactionType)) {
                return transactionType;
            }
        }
        throw new GeneralException(HttpStatus.GONE, INVALID_TRANSACTION_TYPE);
    }

    private List<String> getTransactionStatusQueries(String transactionStatus) {
        if (Strings.isEmpty(transactionStatus)) {
            return Arrays.stream(TransactionStatus.values())
                    .map(Enum::name)
                    .collect(Collectors.toList());
        }

        List<String> result = new ArrayList<>();
        result.add(getTransactionStatusValidated(transactionStatus));
        return result;
    }

    private String getTransactionStatusValidated(String transactionStatus) {
        for (TransactionStatus ts : TransactionStatus.values()) {
            if (ts.name().equals(transactionStatus)) {
                return transactionStatus;
            }
        }
        throw new GeneralException(HttpStatus.GONE, INVALID_TRANSACTION_STATUS);
    }

    @Override
    public ResponseEntity<AcceptWithdrawResponse> acceptWithdraw(AcceptWithdrawRequest request) {
        Long transactionId = request.getTransactionId();
        TransactionHistory transactionHistory = validatorService
                .getPendingWithdrawTransactionValidated(transactionId);

        Long repairerId = transactionHistory.getUserId();
        Long amount = transactionHistory.getAmount();

        Balance balance = balanceDAO.findByUserId(repairerId).get();
        Long oldBalance = balance.getBalance();

        if (oldBalance < amount) {
            throw new GeneralException(HttpStatus.GONE, BALANCE_NOT_ENOUGH);
        }

        balance.setBalance(oldBalance - amount);

        transactionHistory.setStatus(TransactionStatus.SUCCESS.name());

        AcceptWithdrawResponse response = new AcceptWithdrawResponse();
        response.setMessage(ACCEPT_WITHDRAW_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<WithdrawHistoriesResponse> getRepairerWithdrawHistories(WithdrawHistoriesRequest request) {
        int pageNumber = validatorService.getPageNumber(request.getPageNumber());
        int pageSize = validatorService.getPageSize(request.getPageSize());

        int offset = pageNumber * pageSize;

        List<IWithdrawHistoryDTO> repairerWithdrawHistories = transactionHistoryDAO
                .findRepairerWithdrawHistoriesForAdmin(pageSize, offset);
        long totalRecord = transactionHistoryDAO.countByTypeAndStatus(WITHDRAW.name(), PENDING.name());

        WithdrawHistoriesResponse response = new WithdrawHistoriesResponse();
        response.setWithdrawList(repairerWithdrawHistories);
        response.setTotalRecord(totalRecord);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<RejectWithdrawResponse> rejectWithdraw(RejectWithdrawRequest request) {
        String reason = request.getReason();
        if (Strings.isEmpty(reason) || reason.length() > DESCRIPTION_MAX_LENGTH) {
            throw new GeneralException(HttpStatus.GONE, INVALID_REASON);
        }

        Long transactionId = request.getTransactionId();
        TransactionHistory transactionHistory = validatorService
                .getPendingWithdrawTransactionValidated(transactionId);

        transactionHistory.setStatus(FAIL.name());
        transactionHistory.setFailReason(reason);

        RejectWithdrawResponse response = new RejectWithdrawResponse();
        response.setMessage(REJECT_WITHDRAW_REQUEST_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
