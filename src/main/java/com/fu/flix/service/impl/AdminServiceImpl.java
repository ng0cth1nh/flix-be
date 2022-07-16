package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.enums.RoleType;
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fu.flix.constant.Constant.*;
import static com.fu.flix.constant.enums.ActiveState.ACTIVE;
import static com.fu.flix.constant.enums.ActiveState.INACTIVE;

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
    private final AddressService addressService;
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
                            AddressService addressService) {
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
        this.addressService = addressService;
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
        String fullName = request.getFullName();
        if (!InputValidation.isNameValid(fullName, NAME_MAX_LENGTH)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_FULL_NAME);
        }

        String email = request.getEmail();
        if (!InputValidation.isEmailValid(email, true)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_EMAIL);
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
        List<CategoryDTO> categories = categoryPage.stream()
                .map(category -> {
                    Optional<Image> optionalIcon = imageDAO.findById(category.getIconId());
                    Optional<Image> optionalImage = imageDAO.findById(category.getImageId());

                    CategoryDTO dto = new CategoryDTO();
                    dto.setCategoryName(category.getName());
                    dto.setStatus(category.isActive() ? ACTIVE.name() : INACTIVE.name());
                    dto.setId(category.getId());
                    dto.setIcon(optionalIcon.map(Image::getUrl).orElse(null));
                    dto.setImage(optionalImage.map(Image::getUrl).orElse(null));
                    return dto;
                }).collect(Collectors.toList());

        GetCategoriesResponse response = new GetCategoriesResponse();
        response.setCategories(categories);

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
        if (Strings.isEmpty(categoryName)) {
            throw new GeneralException(HttpStatus.GONE, CATEGORY_NAME_IS_REQUIRED);
        }
    }

    private void postCategoryIcon(Category category, MultipartFile icon) throws IOException {
        if (icon != null) {
            String url = cloudStorageService.uploadImage(icon);
            Image savedImage = saveImage(category.getName(), url);
            category.setIconId(savedImage.getId());
        } else {
            category.setIconId(appConf.getDefaultAvatar());
        }
    }

    private void postCategoryImage(Category category, MultipartFile image) throws IOException {
        if (image != null) {
            String url = cloudStorageService.uploadImage(image);
            Image savedImage = saveImage(category.getName(), url);
            category.setImageId(savedImage.getId());
        } else {
            category.setImageId(appConf.getDefaultAvatar());
        }
    }

    @Override
    public ResponseEntity<GetServicesResponse> getServices(GetServicesRequest request) {
        int pageSize = validatorService.getPageSize(request.getPageSize());
        int pageNumber = validatorService.getPageNumber(request.getPageNumber());

        Page<com.fu.flix.entity.Service> servicePage = serviceDAO.findAll(PageRequest.of(pageNumber, pageSize));
        List<ServiceDTO> serviceDTOS = servicePage.stream()
                .map(categoryService::mapToServiceDTO)
                .collect(Collectors.toList());

        GetServicesResponse response = new GetServicesResponse();
        response.setServices(serviceDTOS);

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
        if (Strings.isEmpty(serviceName)) {
            throw new GeneralException(HttpStatus.GONE, SERVICE_NAME_IS_REQUIRED);
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
        if (icon != null) {
            String url = cloudStorageService.uploadImage(icon);
            Image savedImage = saveImage(service.getName(), url);
            service.setIconId(savedImage.getId());
        } else {
            service.setIconId(appConf.getDefaultAvatar());
        }
    }

    private void postServiceImage(com.fu.flix.entity.Service service, MultipartFile image) throws IOException {
        if (image != null) {
            String url = cloudStorageService.uploadImage(image);
            Image savedImage = saveImage(service.getName(), url);
            service.setImageId(savedImage.getId());
        } else {
            service.setImageId(appConf.getDefaultAvatar());
        }
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
        List<AdminSubServiceDTO> subServices = subServicePage.stream()
                .map(subService -> {
                    AdminSubServiceDTO dto = new AdminSubServiceDTO();
                    dto.setId(subService.getId());
                    dto.setSubServiceName(subService.getName());
                    dto.setPrice(subService.getPrice());
                    dto.setStatus(subService.getIsActive() ? ACTIVE.name() : INACTIVE.name());
                    return dto;
                }).collect(Collectors.toList());

        GetSubServicesResponse response = new GetSubServicesResponse();
        response.setSubServices(subServices);

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
        if (Strings.isEmpty(request.getSubServiceName())) {
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

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GetCustomersResponse> getCustomers(GetCustomersRequest request) {
        int pageNumber = validatorService.getPageNumber(request.getPageNumber());
        int pageSize = validatorService.getPageSize(request.getPageSize());

        int offset = pageNumber * pageSize;
        List<ICustomerDTO> customerDTOs = userDAO.findCustomersForAdmin(pageSize, offset);
        GetCustomersResponse response = new GetCustomersResponse();
        response.setCustomerList(customerDTOs);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GetRepairersResponse> getRepairers(GetRepairersRequest request) {
        int pageNumber = validatorService.getPageNumber(request.getPageNumber());
        int pageSize = validatorService.getPageSize(request.getPageSize());

        int offset = pageNumber * pageSize;
        List<IRepairerDTO> repairerDTOS = userDAO.findRepairersForAdmin(pageSize, offset);
        GetRepairersResponse response = new GetRepairersResponse();
        response.setRepairerList(repairerDTOS);

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
        response.setStatus(dto.getStatus());
        response.setDateOfBirth(dob);
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
}
