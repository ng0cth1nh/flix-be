package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.dao.CategoryDAO;
import com.fu.flix.dao.ImageDAO;
import com.fu.flix.dao.ServiceDAO;
import com.fu.flix.dto.CategoryDTO;
import com.fu.flix.dto.ServiceDTO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.entity.Category;
import com.fu.flix.entity.Image;
import com.fu.flix.entity.User;
import com.fu.flix.service.AdminService;
import com.fu.flix.service.CategoryService;
import com.fu.flix.service.CloudStorageService;
import com.fu.flix.service.ValidatorService;
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
    private final Long NAME_MAX_LENGTH;
    private final Long DESCRIPTION_MAX_LENGTH;

    public AdminServiceImpl(ValidatorService validatorService,
                            CategoryDAO categoryDAO,
                            ImageDAO imageDAO,
                            CloudStorageService cloudStorageService,
                            AppConf appConf,
                            ServiceDAO serviceDAO,
                            CategoryService categoryService) {
        this.validatorService = validatorService;
        this.categoryDAO = categoryDAO;
        this.imageDAO = imageDAO;
        this.cloudStorageService = cloudStorageService;
        this.appConf = appConf;
        this.NAME_MAX_LENGTH = appConf.getNameMaxLength();
        this.DESCRIPTION_MAX_LENGTH = appConf.getDescriptionMaxLength();
        this.serviceDAO = serviceDAO;
        this.categoryService = categoryService;
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
}
