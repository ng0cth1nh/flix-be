package com.fu.flix.service.impl;

import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
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
    void test_create_category_fail_when_description_length_is_2501() {
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
    void test_create_category_fail_when_category_name_is_null() {
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

    @Test
    void test_update_category_success() throws IOException {
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

        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setIcon(icon);
        request.setImage(image);
        request.setDescription("fake description 1");
        request.setCategoryName("Fake category 8");
        request.setIsActive(true);
        request.setId(14L);

        setManagerContext(438L, "0865390063");

        // when
        UpdateCategoryResponse response = underTest.updateCategory(request).getBody();

        // then
        Assertions.assertEquals(UPDATE_CATEGORY_SUCCESS, response.getMessage());
    }

    @Test
    void test_update_category_success_when_active_is_null() throws IOException {
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

        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setIcon(icon);
        request.setImage(image);
        request.setDescription("fake description 1");
        request.setCategoryName("Fake category 8");
        request.setIsActive(null);
        request.setId(14L);

        setManagerContext(438L, "0865390063");

        // when
        UpdateCategoryResponse response = underTest.updateCategory(request).getBody();

        // then
        Assertions.assertEquals(UPDATE_CATEGORY_SUCCESS, response.getMessage());
    }

    @Test
    void test_get_services_success() {
        // given
        GetServicesRequest request = new GetServicesRequest();
        request.setPageNumber(0);
        request.setPageSize(5);

        setManagerContext(438L, "0865390063");

        // when
        GetServicesResponse response = underTest.getServices(request).getBody();

        // then
        Assertions.assertEquals(5, response.getServices().size());
    }

    @Test
    void test_create_service_success() throws IOException {
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

        CreateServiceRequest request = new CreateServiceRequest();
        request.setIcon(icon);
        request.setImage(image);
        request.setDescription("fake description 1");
        request.setServiceName("Fake Service 8");
        request.setIsActive(true);
        request.setInspectionPrice(20000L);
        request.setCategoryId(5L);

        setManagerContext(438L, "0865390063");

        // when
        CreateServiceResponse response = underTest.createService(request).getBody();

        // then
        Assertions.assertEquals(CREATE_SERVICE_SUCCESS, response.getMessage());
    }

    @Test
    void test_create_service_success_when_image_and_icon_are_null() throws IOException {
        // given
        CreateServiceRequest request = new CreateServiceRequest();
        request.setIcon(null);
        request.setImage(null);
        request.setDescription("fake description 1");
        request.setServiceName("Fake Service 8");
        request.setIsActive(true);
        request.setInspectionPrice(20000L);
        request.setCategoryId(5L);

        setManagerContext(438L, "0865390063");

        // when
        CreateServiceResponse response = underTest.createService(request).getBody();

        // then
        Assertions.assertEquals(CREATE_SERVICE_SUCCESS, response.getMessage());
    }

    @Test
    void test_create_service_success_when_active_is_null() throws IOException {
        // given
        CreateServiceRequest request = new CreateServiceRequest();
        request.setIcon(null);
        request.setImage(null);
        request.setDescription("fake description 1");
        request.setServiceName("Fake Service 8");
        request.setIsActive(null);
        request.setInspectionPrice(20000L);
        request.setCategoryId(5L);

        setManagerContext(438L, "0865390063");

        // when
        CreateServiceResponse response = underTest.createService(request).getBody();

        // then
        Assertions.assertEquals(CREATE_SERVICE_SUCCESS, response.getMessage());
    }

    @Test
    void test_create_service_fail_when_description_length_is_2501() {
        // given
        CreateServiceRequest request = new CreateServiceRequest();
        request.setIcon(null);
        request.setImage(null);
        request.setDescription("a".repeat(2501));
        request.setServiceName("Fake Service 8");
        request.setIsActive(null);
        request.setInspectionPrice(20000L);
        request.setCategoryId(5L);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createService(request));

        // then
        Assertions.assertEquals(EXCEEDED_DESCRIPTION_LENGTH_ALLOWED, exception.getMessage());
    }

    @Test
    void test_create_service_fail_when_service_name_is_null() {
        // given
        CreateServiceRequest request = new CreateServiceRequest();
        request.setIcon(null);
        request.setImage(null);
        request.setDescription("a");
        request.setServiceName(null);
        request.setIsActive(null);
        request.setInspectionPrice(20000L);
        request.setCategoryId(5L);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createService(request));

        // then
        Assertions.assertEquals(INVALID_SERVICE_NAME, exception.getMessage());
    }

    @Test
    void test_create_service_fail_when_inspection_price_is_null() {
        // given
        CreateServiceRequest request = new CreateServiceRequest();
        request.setIcon(null);
        request.setImage(null);
        request.setDescription("a");
        request.setServiceName("la la");
        request.setIsActive(null);
        request.setInspectionPrice(null);
        request.setCategoryId(5L);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createService(request));

        // then
        Assertions.assertEquals(INVALID_INSPECTION_PRICE, exception.getMessage());
    }

    @Test
    void test_create_service_fail_when_category_id_is_null() {
        // given
        CreateServiceRequest request = new CreateServiceRequest();
        request.setIcon(null);
        request.setImage(null);
        request.setDescription("a");
        request.setServiceName("la la");
        request.setIsActive(null);
        request.setInspectionPrice(20000L);
        request.setCategoryId(null);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createService(request));

        // then
        Assertions.assertEquals(CATEGORY_ID_IS_REQUIRED, exception.getMessage());
    }

    @Test
    void test_create_service_fail_when_category_not_found() {
        // given
        CreateServiceRequest request = new CreateServiceRequest();
        request.setIcon(null);
        request.setImage(null);
        request.setDescription("a");
        request.setServiceName("la la");
        request.setIsActive(null);
        request.setInspectionPrice(20000L);
        request.setCategoryId(1000000L);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createService(request));

        // then
        Assertions.assertEquals(CATEGORY_NOT_FOUND, exception.getMessage());
    }

    @Test
    void test_update_service_success() throws IOException {
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

        UpdateServiceRequest request = new UpdateServiceRequest();
        request.setIcon(icon);
        request.setImage(image);
        request.setDescription("fake description 1");
        request.setServiceName("Fake Service 8");
        request.setIsActive(true);
        request.setInspectionPrice(20000L);
        request.setCategoryId(5L);
        request.setServiceId(1L);

        setManagerContext(438L, "0865390063");

        // when
        UpdateServiceResponse response = underTest.updateService(request).getBody();

        // then
        Assertions.assertEquals(UPDATE_SERVICE_SUCCESS, response.getMessage());
    }

    @Test
    void test_update_service_success_when_active_is_null() throws IOException {
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

        UpdateServiceRequest request = new UpdateServiceRequest();
        request.setIcon(icon);
        request.setImage(image);
        request.setDescription("fake description 1");
        request.setServiceName("Fake Service 8");
        request.setIsActive(null);
        request.setInspectionPrice(20000L);
        request.setCategoryId(5L);
        request.setServiceId(1L);

        setManagerContext(438L, "0865390063");

        // when
        UpdateServiceResponse response = underTest.updateService(request).getBody();

        // then
        Assertions.assertEquals(UPDATE_SERVICE_SUCCESS, response.getMessage());
    }

    @Test
    void test_search_services_success() {
        // given
        AdminSearchServicesRequest request = new AdminSearchServicesRequest();
        request.setKeyword("điện");

        setManagerContext(438L, "0865390063");

        // when
        AdminSearchServicesResponse response = underTest.searchServices(request).getBody();

        // then
        Assertions.assertNotNull(response);
    }

    @Test
    void test_search_services_fail_when_keyword_is_null() {
        // given
        AdminSearchServicesRequest request = new AdminSearchServicesRequest();
        request.setKeyword(null);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.searchServices(request));

        // then
        Assertions.assertEquals(INVALID_KEY_WORD, exception.getMessage());
    }

    @Test
    void test_get_sub_services_success() {
        // given
        GetSubServicesRequest request = new GetSubServicesRequest();
        request.setPageNumber(0);
        request.setPageSize(5);

        setManagerContext(438L, "0865390063");

        // when
        GetSubServicesResponse response = underTest.getSubServices(request).getBody();

        // then
        Assertions.assertEquals(5, response.getSubServices().size());
    }

    @Test
    void test_create_sub_service_success() {
        // given
        CreateSubServiceRequest request = new CreateSubServiceRequest();
        request.setSubServiceName("Fake Sub Service 5");
        request.setPrice(33000L);
        request.setServiceId(2L);
        request.setDescription("Fake description 5");
        request.setIsActive(true);

        setManagerContext(438L, "0865390063");

        // when
        CreateSubServiceResponse response = underTest.createSubService(request).getBody();

        // then
        Assertions.assertEquals(CREATE_SUB_SERVICE_SUCCESS, response.getMessage());
    }

    @Test
    void test_create_sub_service_success_when_active_is_null() {
        // given
        CreateSubServiceRequest request = new CreateSubServiceRequest();
        request.setSubServiceName("Fake Sub Service 5");
        request.setPrice(33000L);
        request.setServiceId(2L);
        request.setDescription("Fake description 5");
        request.setIsActive(null);

        setManagerContext(438L, "0865390063");

        // when
        CreateSubServiceResponse response = underTest.createSubService(request).getBody();

        // then
        Assertions.assertEquals(CREATE_SUB_SERVICE_SUCCESS, response.getMessage());
    }

    @Test
    void test_create_sub_service_fail_when_sub_service_name_is_null() {
        // given
        CreateSubServiceRequest request = new CreateSubServiceRequest();
        request.setSubServiceName(null);
        request.setPrice(33000L);
        request.setServiceId(2L);
        request.setDescription("Fake description 5");
        request.setIsActive(true);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createSubService(request));

        // then
        Assertions.assertEquals(INVALID_SUB_SERVICE_NAME, exception.getMessage());
    }

    @Test
    void test_create_sub_service_fail_when_price_is_null() {
        // given
        CreateSubServiceRequest request = new CreateSubServiceRequest();
        request.setSubServiceName("la la");
        request.setPrice(null);
        request.setServiceId(2L);
        request.setDescription("Fake description 5");
        request.setIsActive(true);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createSubService(request));

        // then
        Assertions.assertEquals(INVALID_PRICE, exception.getMessage());
    }

    @Test
    void test_create_sub_service_fail_when_description_length_is_2501() {
        // given
        CreateSubServiceRequest request = new CreateSubServiceRequest();
        request.setSubServiceName("la la");
        request.setPrice(33000L);
        request.setServiceId(2L);
        request.setDescription("a".repeat(2501));
        request.setIsActive(true);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createSubService(request));

        // then
        Assertions.assertEquals(EXCEEDED_DESCRIPTION_LENGTH_ALLOWED, exception.getMessage());
    }

    @Test
    void test_update_sub_service_success() {
        // given
        UpdateSubServiceRequest request = new UpdateSubServiceRequest();
        request.setSubServiceName("la la");
        request.setPrice(33000L);
        request.setServiceId(3L);
        request.setDescription("meo meo");
        request.setIsActive(true);
        request.setSubServiceId(295L);

        setManagerContext(438L, "0865390063");

        // when
        UpdateSubServiceResponse response = underTest.updateSubService(request).getBody();

        // then
        Assertions.assertEquals(UPDATE_SUB_SERVICE_SUCCESS, response.getMessage());
    }

    @Test
    void test_update_sub_service_fail_when_sub_service_is_null() {
        // given
        UpdateSubServiceRequest request = new UpdateSubServiceRequest();
        request.setSubServiceName("la la");
        request.setPrice(33000L);
        request.setServiceId(3L);
        request.setDescription("meo meo");
        request.setIsActive(true);
        request.setSubServiceId(null);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.updateSubService(request));

        // then
        Assertions.assertEquals(INVALID_SUB_SERVICE, exception.getMessage());
    }

    @Test
    void test_update_sub_service_fail_when_sub_service_is_not_found() {
        // given
        UpdateSubServiceRequest request = new UpdateSubServiceRequest();
        request.setSubServiceName("la la");
        request.setPrice(33000L);
        request.setServiceId(3L);
        request.setDescription("meo meo");
        request.setIsActive(true);
        request.setSubServiceId(1000000L);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.updateSubService(request));

        // then
        Assertions.assertEquals(INVALID_SUB_SERVICE, exception.getMessage());
    }

    @Test
    void test_update_sub_service_success_when_active_is_null() {
        // given
        UpdateSubServiceRequest request = new UpdateSubServiceRequest();
        request.setSubServiceName("la la");
        request.setPrice(33000L);
        request.setServiceId(3L);
        request.setDescription("meo meo");
        request.setIsActive(null);
        request.setSubServiceId(295L);

        setManagerContext(438L, "0865390063");

        // when
        UpdateSubServiceResponse response = underTest.updateSubService(request).getBody();

        // then
        Assertions.assertEquals(UPDATE_SUB_SERVICE_SUCCESS, response.getMessage());
    }

    @Test
    void test_get_requests_success() {
        // given
        AdminRequestingRequest request = new AdminRequestingRequest();
        request.setPageNumber(0);
        request.setPageSize(5);

        setManagerContext(438L, "0865390063");

        // when
        AdminRequestingResponse response = underTest.getRequests(request).getBody();

        // then
        Assertions.assertEquals(5, response.getRequestList().size());
    }

    @Test
    void test_get_customers_success() {
        // given
        GetCustomersRequest request = new GetCustomersRequest();
        request.setPageNumber(0);
        request.setPageSize(5);

        setManagerContext(438L, "0865390063");

        // when
        GetCustomersResponse response = underTest.getCustomers(request).getBody();

        // then
        Assertions.assertEquals(5, response.getCustomerList().size());
    }

    @Test
    void test_get_repairers_success() {
        // given
        GetRepairersRequest request = new GetRepairersRequest();
        request.setPageNumber(0);
        request.setPageSize(5);

        setManagerContext(438L, "0865390063");

        // when
        GetRepairersResponse response = underTest.getRepairers(request).getBody();

        // then
        Assertions.assertEquals(5, response.getRepairerList().size());
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