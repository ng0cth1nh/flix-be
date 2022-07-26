package com.fu.flix.service.impl;

import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.CreateCategoryRequest;
import com.fu.flix.dto.request.GetAdminProfileRequest;
import com.fu.flix.dto.request.GetCategoriesRequest;
import com.fu.flix.dto.request.UpdateAdminProfileRequest;
import com.fu.flix.dto.response.CreateCategoryResponse;
import com.fu.flix.dto.response.GetAdminProfileResponse;
import com.fu.flix.dto.response.GetCategoriesResponse;
import com.fu.flix.dto.response.UpdateAdminProfileResponse;
import com.fu.flix.dto.security.UserPrincipal;
import com.fu.flix.service.AdminService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static com.fu.flix.constant.Constant.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class AdminServiceImplTest {

    @Autowired
    AdminService underTest;

    @Test
    void test_get_admin_profile_success() {
        // given
        GetAdminProfileRequest request = new GetAdminProfileRequest();
        setManagerContext(438L, "0865390063");

        // when
        GetAdminProfileResponse response = underTest.getAdminProfile(request).getBody();

        // then
        Assertions.assertNotNull(response);
    }

    @Test
    void test_update_admin_profile_success() {
        // given
        UpdateAdminProfileRequest request = new UpdateAdminProfileRequest();
        request.setFullName("Chí Dũng");
        request.setEmail("admin123@gmail.com");

        setManagerContext(438L, "0865390063");

        // when
        UpdateAdminProfileResponse response = underTest.updateAdminProfile(request).getBody();

        // then
        Assertions.assertEquals(UPDATE_ADMIN_PROFILE_SUCCESS, response.getMessage());
    }

    @Test
    void test_update_admin_profile_fail_when_invalid_email() {
        // given
        UpdateAdminProfileRequest request = new UpdateAdminProfileRequest();
        request.setFullName("Chí Dũng");
        request.setEmail("123");

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.updateAdminProfile(request));

        // then
        Assertions.assertEquals(INVALID_EMAIL, exception.getMessage());
    }

    @Test
    void test_update_admin_profile_fail_when_full_name_is_null() {
        // given
        UpdateAdminProfileRequest request = new UpdateAdminProfileRequest();
        request.setFullName(null);
        request.setEmail("admin123@gmail.com");

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.updateAdminProfile(request));

        // then
        Assertions.assertEquals(INVALID_FULL_NAME, exception.getMessage());
    }

    @Test
    void test_get_categories_success() {
        // given
        GetCategoriesRequest request = new GetCategoriesRequest();
        request.setPageNumber(0);
        request.setPageSize(5);

        setManagerContext(438L, "0865390063");

        // when
        GetCategoriesResponse response = underTest.getCategories(request).getBody();

        // then
        Assertions.assertEquals(5, response.getCategories().size());
    }

    @Test
    void test_create_category_success() throws IOException {
        // given
        MockMultipartFile icon = new MockMultipartFile(
                "icon",
                "icon.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "icon".getBytes());

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "image".getBytes());

        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setIcon(icon);
        request.setImage(image);
        request.setDescription("fake description 1");
        request.setCategoryName("Fake category 8");
        request.setIsActive(true);

        setManagerContext(438L, "0865390063");

        // when
        CreateCategoryResponse response = underTest.createCategory(request).getBody();

        // then
        Assertions.assertEquals(CREATE_CATEGORY_SUCCESS, response.getMessage());
    }

    @Test
    void test_create_category_success_when_icon_and_image_are_null() throws IOException {
        // given
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setIcon(null);
        request.setImage(null);
        request.setDescription("fake description 1");
        request.setCategoryName("Fake category 8");
        request.setIsActive(true);

        setManagerContext(438L, "0865390063");

        // when
        CreateCategoryResponse response = underTest.createCategory(request).getBody();

        // then
        Assertions.assertEquals(CREATE_CATEGORY_SUCCESS, response.getMessage());
    }

    @Test
    void test_create_category_success_when_active_is_null() throws IOException {
        // given
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setIcon(null);
        request.setImage(null);
        request.setDescription("fake description 1");
        request.setCategoryName("Fake category 8");
        request.setIsActive(null);

        setManagerContext(438L, "0865390063");

        // when
        CreateCategoryResponse response = underTest.createCategory(request).getBody();

        // then
        Assertions.assertEquals(CREATE_CATEGORY_SUCCESS, response.getMessage());
    }

    @Test
    void test_create_category_fail_when_description_length_is_2501() throws IOException {
        // given
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setIcon(null);
        request.setImage(null);
        request.setDescription("a".repeat(2501));
        request.setCategoryName("Fake category 8");
        request.setIsActive(null);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createCategory(request));

        // then
        Assertions.assertEquals(EXCEEDED_DESCRIPTION_LENGTH_ALLOWED, exception.getMessage());
    }

    @Test
    void test_create_category_fail_when_category_name_is_null() throws IOException {
        // given
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setIcon(null);
        request.setImage(null);
        request.setDescription("fake description 1");
        request.setCategoryName(null);
        request.setIsActive(null);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createCategory(request));

        // then
        Assertions.assertEquals(INVALID_CATEGORY_NAME, exception.getMessage());
    }

    void setManagerContext(Long id, String phone) {
        String[] roles = {"ROLE_MANAGER"};
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(new UserPrincipal(id, phone, roles), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}