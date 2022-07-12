package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.dao.CategoryDAO;
import com.fu.flix.dao.ImageDAO;
import com.fu.flix.dto.CategoryDTO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.CreateCategoryRequest;
import com.fu.flix.dto.request.GetAdminProfileRequest;
import com.fu.flix.dto.request.GetCategoriesRequest;
import com.fu.flix.dto.request.UpdateAdminProfileRequest;
import com.fu.flix.dto.response.CreateCategoryResponse;
import com.fu.flix.dto.response.GetAdminProfileResponse;
import com.fu.flix.dto.response.GetCategoriesResponse;
import com.fu.flix.dto.response.UpdateAdminProfileResponse;
import com.fu.flix.entity.Category;
import com.fu.flix.entity.Image;
import com.fu.flix.entity.User;
import com.fu.flix.service.AdminService;
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
    private final Long NAME_MAX_LENGTH;
    private final Long DESCRIPTION_MAX_LENGTH;

    public AdminServiceImpl(ValidatorService validatorService,
                            CategoryDAO categoryDAO,
                            ImageDAO imageDAO,
                            CloudStorageService cloudStorageService,
                            AppConf appConf) {
        this.validatorService = validatorService;
        this.categoryDAO = categoryDAO;
        this.imageDAO = imageDAO;
        this.cloudStorageService = cloudStorageService;
        this.appConf = appConf;
        this.NAME_MAX_LENGTH = appConf.getNameMaxLength();
        this.DESCRIPTION_MAX_LENGTH = appConf.getDescriptionMaxLength();
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

                    CategoryDTO dto = new CategoryDTO();
                    dto.setCategoryName(category.getName());
                    dto.setStatus(category.isActive() ? ACTIVE.name() : INACTIVE.name());
                    dto.setId(category.getId());
                    dto.setIcon(optionalIcon.map(Image::getUrl).orElse(null));
                    return dto;
                }).collect(Collectors.toList());

        GetCategoriesResponse response = new GetCategoriesResponse();
        response.setCategories(categories);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CreateCategoryResponse> createCategory(CreateCategoryRequest request) throws IOException {
        String description = request.getDescription();
        if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new GeneralException(HttpStatus.GONE, EXCEEDED_DESCRIPTION_LENGTH_ALLOWED);
        }

        String categoryName = request.getCategoryName();
        if (Strings.isEmpty(categoryName)) {
            throw new GeneralException(HttpStatus.GONE, CATEGORY_NAME_IS_REQUIRED);
        }

        boolean isActive = request.getIsActive() != null
                ? request.getIsActive()
                : true;

        Category category = new Category();
        category.setName(categoryName);
        category.setDescription(description);
        category.setActive(isActive);
        postCategoryIcon(category, request.getIcon());
        categoryDAO.save(category);

        CreateCategoryResponse response = new CreateCategoryResponse();
        response.setMessage(CREATE_CATEGORY_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void postCategoryIcon(Category category, MultipartFile icon) throws IOException {
        if (icon != null) {
            String url = cloudStorageService.uploadImage(icon);

            Image image = new Image();
            image.setName(category.getName());
            image.setUrl(url);
            Image savedImage = imageDAO.save(image);

            category.setIconId(savedImage.getId());
        } else {
            category.setIconId(appConf.getDefaultAvatar());
        }
    }
}
