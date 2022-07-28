package com.fu.flix.service.impl;

import com.fu.flix.dao.UserDAO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.dto.security.UserPrincipal;
import com.fu.flix.entity.User;
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
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.fu.flix.constant.Constant.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class AdminServiceImplTest {

    @Autowired
    AdminService underTest;

    @Autowired
    UserDAO userDAO;

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

    @Test
    void test_get_customer_detail_success() {
        // given
        GetCustomerDetailRequest request = new GetCustomerDetailRequest();
        request.setCustomerId(36L);

        setManagerContext(438L, "0865390063");

        // when
        GetCustomerDetailResponse response = underTest.getCustomerDetail(request).getBody();

        // then
        Assertions.assertNotNull(response);
    }

    @Test
    void test_get_customer_detail_fail_when_customer_is_not_found() {
        // given
        GetCustomerDetailRequest request = new GetCustomerDetailRequest();
        request.setCustomerId(1000000L);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getCustomerDetail(request));

        // then
        Assertions.assertEquals(CUSTOMER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void test_get_customer_detail_success_when_dob_not_null() {
        // given
        GetCustomerDetailRequest request = new GetCustomerDetailRequest();
        request.setCustomerId(36L);

        User user = userDAO.findById(36L).get();
        user.setDateOfBirth(null);

        setManagerContext(438L, "0865390063");

        // when
        GetCustomerDetailResponse response = underTest.getCustomerDetail(request).getBody();

        // then
        Assertions.assertNotNull(response);
    }

    @Test
    void test_get_ban_users_success() {
        // given
        GetBanUsersRequest request = new GetBanUsersRequest();
        request.setPageNumber(0);
        request.setPageSize(2);

        setManagerContext(438L, "0865390063");

        // when
        GetBanUsersResponse response = underTest.getBanUsers(request).getBody();

        // then
        Assertions.assertEquals(2, response.getUserList().size());
    }

    @Test
    void test_ban_customer_success() {
        // given
        BanUserRequest request = new BanUserRequest();
        request.setPhone("0865390040");
        request.setBanReason("Thích thì cấm tài khoản thôi");

        setManagerContext(438L, "0865390063");

        // when
        BanUserResponse response = underTest.banUser(request).getBody();

        // then
        Assertions.assertEquals(BAN_USER_SUCCESS, response.getMessage());
    }

    @Test
    void test_ban_repairer_success() {
        // given
        BanUserRequest request = new BanUserRequest();
        request.setPhone("0865390051");
        request.setBanReason("Thích thì cấm tài khoản thôi");

        setManagerContext(438L, "0865390063");

        // when
        BanUserResponse response = underTest.banUser(request).getBody();

        // then
        Assertions.assertEquals(BAN_USER_SUCCESS, response.getMessage());
    }

    @Test
    void test_ban_pending_repairer_success() {
        // given
        BanUserRequest request = new BanUserRequest();
        request.setPhone("0865390060");
        request.setBanReason("Thích thì cấm tài khoản thôi");

        setManagerContext(438L, "0865390063");

        // when
        BanUserResponse response = underTest.banUser(request).getBody();

        // then
        Assertions.assertEquals(BAN_USER_SUCCESS, response.getMessage());
    }

    @Test
    void test_ban_user_fail_when_role_is_admin() {
        // given
        BanUserRequest request = new BanUserRequest();
        request.setPhone("0865390063");
        request.setBanReason("Thích thì cấm tài khoản thôi");

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.banUser(request));

        // then
        Assertions.assertEquals(JUST_CAN_BAN_USER_ROLE_ARE_CUSTOMER_OR_REPAIRER_OR_PENDING_REPAIRER, exception.getMessage());
    }

    @Test
    void test_ban_user_fail_when_invalid_phone() {
        // given
        BanUserRequest request = new BanUserRequest();
        request.setPhone("123");
        request.setBanReason("Thích thì cấm tài khoản thôi");

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.banUser(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    void test_ban_user_fail_when_ban_reason_is_null() {
        // given
        BanUserRequest request = new BanUserRequest();
        request.setPhone("0865390060");
        request.setBanReason(null);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.banUser(request));

        // then
        Assertions.assertEquals(INVALID_BAN_REASON, exception.getMessage());
    }

    @Test
    void test_ban_user_fail_when_user_not_found() {
        // given
        BanUserRequest request = new BanUserRequest();
        request.setPhone("0865111111");
        request.setBanReason("meo meo");

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.banUser(request));

        // then
        Assertions.assertEquals(USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void test_ban_user_fail_when_user_has_been_banned() {
        // given
        BanUserRequest request = new BanUserRequest();
        request.setPhone("0865390041");
        request.setBanReason("meo meo");

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.banUser(request));

        // then
        Assertions.assertEquals(THIS_ACCOUNT_HAS_BEEN_BANNED, exception.getMessage());
    }

    @Test
    void test_create_feedback_success() throws IOException {
        // given
        MockMultipartFile image = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());

        List<MultipartFile> images = new ArrayList<>();
        images.add(image);

        AdminCreateFeedBackRequest request = new AdminCreateFeedBackRequest();
        request.setPhone("0585943270");
        request.setFeedbackType("COMMENT");
        request.setRequestCode("F9LF7G9Z27SE");
        request.setTitle("Chán cái App App thật sự");
        request.setDescription("app rác này liệu có được được 10");
        request.setImages(images);

        setManagerContext(438L, "0865390063");

        // when
        AdminCreateFeedBackResponse response = underTest.createFeedback(request).getBody();

        // then
        Assertions.assertEquals(CREATE_FEEDBACK_SUCCESS, response.getMessage());
    }

    @Test
    void test_create_feedback_fail_when_phone_is_123() {
        // given
        AdminCreateFeedBackRequest request = new AdminCreateFeedBackRequest();
        request.setPhone("123");
        request.setFeedbackType("COMMENT");
        request.setRequestCode("F9LF7G9Z27SE");
        request.setTitle("Chán cái App App thật sự");
        request.setDescription("app rác này liệu có được được 10");

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFeedback(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    void test_create_feedback_fail_when_user_not_found() {
        // given
        AdminCreateFeedBackRequest request = new AdminCreateFeedBackRequest();
        request.setPhone("0865111111");
        request.setFeedbackType("COMMENT");
        request.setRequestCode("F9LF7G9Z27SE");
        request.setTitle("Chán cái App App thật sự");
        request.setDescription("app rác này liệu có được được 10");

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFeedback(request));

        // then
        Assertions.assertEquals(USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void test_create_feedback_fail_when_invalid_request_code() {
        // given
        AdminCreateFeedBackRequest request = new AdminCreateFeedBackRequest();
        request.setPhone("0585943270");
        request.setFeedbackType("COMMENT");
        request.setRequestCode("123");
        request.setTitle("Chán cái App App thật sự");
        request.setDescription("app rác này liệu có được được 10");

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFeedback(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    void test_create_feedback_fail_when_title_is_null() {
        // given
        AdminCreateFeedBackRequest request = new AdminCreateFeedBackRequest();
        request.setPhone("0585943270");
        request.setFeedbackType("COMMENT");
        request.setRequestCode("F9LF7G9Z27SE");
        request.setTitle(null);
        request.setDescription("app rác này liệu có được được 10");

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFeedback(request));

        // then
        Assertions.assertEquals(INVALID_TITLE, exception.getMessage());
    }

    @Test
    void test_create_feedback_fail_when_description_is_null() {
        // given
        AdminCreateFeedBackRequest request = new AdminCreateFeedBackRequest();
        request.setPhone("0585943270");
        request.setFeedbackType("COMMENT");
        request.setRequestCode("F9LF7G9Z27SE");
        request.setTitle("lala la");
        request.setDescription(null);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFeedback(request));

        // then
        Assertions.assertEquals(INVALID_DESCRIPTION, exception.getMessage());
    }

    @Test
    void test_get_feedback_detail_success() {
        // given
        FeedbackDetailRequest request = new FeedbackDetailRequest();
        request.setFeedbackId(20L);

        setManagerContext(438L, "0865390063");

        // when
        FeedbackDetailResponse response = underTest.getFeedbackDetail(request).getBody();

        // then
        Assertions.assertNotNull(response);
    }

    @Test
    void test_get_feedback_detail_fail_when_feedback_id_is_null() {
        // given
        FeedbackDetailRequest request = new FeedbackDetailRequest();
        request.setFeedbackId(null);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getFeedbackDetail(request));

        // then
        Assertions.assertEquals(FEEDBACK_ID_IS_REQUIRED, exception.getMessage());
    }

    @Test
    void test_get_feedback_detail_fail_when_feedback_is_not_found() {
        // given
        FeedbackDetailRequest request = new FeedbackDetailRequest();
        request.setFeedbackId(1000000L);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getFeedbackDetail(request));

        // then
        Assertions.assertEquals(INVALID_FEEDBACK_ID, exception.getMessage());
    }

    @Test
    void test_get_accessories_success() {
        // given
        AdminGetAccessoriesRequest request = new AdminGetAccessoriesRequest();
        request.setPageNumber(0);
        request.setPageSize(5);

        setManagerContext(438L, "0865390063");

        // when
        AdminGetAccessoriesResponse response = underTest.getAccessories(request).getBody();

        // then
        Assertions.assertEquals(5, response.getAccessoryList().size());
    }

    @Test
    void test_get_pending_repairer_success() {
        // given
        PendingRepairersRequest request = new PendingRepairersRequest();
        request.setPageNumber(0);
        request.setPageSize(2);

        setManagerContext(438L, "0865390063");

        // when
        PendingRepairersResponse response = underTest.getPendingRepairers(request).getBody();

        // then
        Assertions.assertEquals(2, response.getRepairerList().size());
    }

    @Test
    void test_create_accessory_success() {
        // given
        CreateAccessoryRequest request = new CreateAccessoryRequest();
        request.setAccessoryName("Linh kiện fake của tủ lạnh");
        request.setPrice(20000L);
        request.setInsurance(18);
        request.setManufacturer("Honda");
        request.setCountry("Nhật");
        request.setDescription("Cực xịn");
        request.setServiceId(3L);

        setManagerContext(438L, "0865390063");

        // when
        CreateAccessoryResponse response = underTest.createAccessory(request).getBody();

        // then
        Assertions.assertEquals(CREATE_ACCESSORY_SUCCESS, response.getMessage());
    }

    @Test
    void test_create_accessory_fail_when_price_is_null() {
        // given
        CreateAccessoryRequest request = new CreateAccessoryRequest();
        request.setAccessoryName("Linh kiện fake của tủ lạnh");
        request.setPrice(null);
        request.setInsurance(18);
        request.setManufacturer("Honda");
        request.setCountry("Nhật");
        request.setDescription("Cực xịn");
        request.setServiceId(3L);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createAccessory(request));

        // then
        Assertions.assertEquals(INVALID_PRICE, exception.getMessage());
    }

    @Test
    void test_create_accessory_fail_when_invalid_insurance() {
        // given
        CreateAccessoryRequest request = new CreateAccessoryRequest();
        request.setAccessoryName("Linh kiện fake của tủ lạnh");
        request.setPrice(20000L);
        request.setInsurance(-1);
        request.setManufacturer("Honda");
        request.setCountry("Nhật");
        request.setDescription("Cực xịn");
        request.setServiceId(3L);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createAccessory(request));

        // then
        Assertions.assertEquals(INVALID_INSURANCE, exception.getMessage());
    }

    @Test
    void test_create_accessory_fail_when_description_length_is_2501() {
        // given
        CreateAccessoryRequest request = new CreateAccessoryRequest();
        request.setAccessoryName("Linh kiện fake của tủ lạnh");
        request.setPrice(20000L);
        request.setInsurance(18);
        request.setManufacturer("Honda");
        request.setCountry("Nhật");
        request.setDescription("a".repeat(2501));
        request.setServiceId(3L);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createAccessory(request));

        // then
        Assertions.assertEquals(EXCEEDED_DESCRIPTION_LENGTH_ALLOWED, exception.getMessage());
    }

    @Test
    void test_create_accessory_fail_when_accessory_name_is_null() {
        // given
        CreateAccessoryRequest request = new CreateAccessoryRequest();
        request.setAccessoryName(null);
        request.setPrice(20000L);
        request.setInsurance(18);
        request.setManufacturer("Honda");
        request.setCountry("Nhật");
        request.setDescription("la la");
        request.setServiceId(3L);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createAccessory(request));

        // then
        Assertions.assertEquals(INVALID_ACCESSORY_NAME, exception.getMessage());
    }

    @Test
    void test_create_accessory_fail_when_service_id_is_null() {
        // given
        CreateAccessoryRequest request = new CreateAccessoryRequest();
        request.setAccessoryName("la la");
        request.setPrice(20000L);
        request.setInsurance(18);
        request.setManufacturer("Honda");
        request.setCountry("Nhật");
        request.setDescription("la la");
        request.setServiceId(null);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createAccessory(request));

        // then
        Assertions.assertEquals(SERVICE_ID_IS_REQUIRED, exception.getMessage());
    }

    @Test
    void test_create_accessory_fail_when_service_id_is_not_found() {
        // given
        CreateAccessoryRequest request = new CreateAccessoryRequest();
        request.setAccessoryName("la la");
        request.setPrice(20000L);
        request.setInsurance(18);
        request.setManufacturer("Honda");
        request.setCountry("Nhật");
        request.setDescription("la la");
        request.setServiceId(1000000L);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createAccessory(request));

        // then
        Assertions.assertEquals(INVALID_SERVICE, exception.getMessage());
    }

    @Test
    void test_update_accessory_success() {
        // given
        UpdateAccessoryRequest request = new UpdateAccessoryRequest();
        request.setAccessoryName("Linh kiện fake của tủ lạnh");
        request.setPrice(20000L);
        request.setInsurance(18);
        request.setManufacturer("Honda");
        request.setCountry("Nhật");
        request.setDescription("Cực xịn");
        request.setServiceId(3L);
        request.setId(8L);

        setManagerContext(438L, "0865390063");

        // when
        UpdateAccessoryResponse response = underTest.updateAccessory(request).getBody();

        // then
        Assertions.assertEquals(UPDATE_ACCESSORY_SUCCESS, response.getMessage());
    }

    @Test
    void test_update_accessory_fail_when_accessory_id_is_null() {
        // given
        UpdateAccessoryRequest request = new UpdateAccessoryRequest();
        request.setAccessoryName("Linh kiện fake của tủ lạnh");
        request.setPrice(20000L);
        request.setInsurance(18);
        request.setManufacturer("Honda");
        request.setCountry("Nhật");
        request.setDescription("Cực xịn");
        request.setServiceId(3L);
        request.setId(null);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.updateAccessory(request));

        // then
        Assertions.assertEquals(INVALID_ACCESSORY, exception.getMessage());
    }

    @Test
    void test_update_accessory_fail_when_accessory_id_is_invalid() {
        // given
        UpdateAccessoryRequest request = new UpdateAccessoryRequest();
        request.setAccessoryName("Linh kiện fake của tủ lạnh");
        request.setPrice(20000L);
        request.setInsurance(18);
        request.setManufacturer("Honda");
        request.setCountry("Nhật");
        request.setDescription("Cực xịn");
        request.setServiceId(3L);
        request.setId(1000000L);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.updateAccessory(request));

        // then
        Assertions.assertEquals(INVALID_ACCESSORY, exception.getMessage());
    }

    @Test
    void test_response_feedback_success() {
        // given
        ResponseFeedbackRequest request = new ResponseFeedbackRequest();
        request.setId(1L);
        request.setStatus("PROCESSING");
        request.setResponse("Đang xử lí, mày chờ tý");

        setManagerContext(438L, "0865390063");

        // when
        ResponseFeedbackResponse response = underTest.responseFeedback(request).getBody();

        // then
        Assertions.assertEquals(RESPONSE_FEEDBACK_SUCCESS, response.getMessage());
    }

    @Test
    void test_response_feedback_fail_when_response_is_null() {
        // given
        ResponseFeedbackRequest request = new ResponseFeedbackRequest();
        request.setId(1L);
        request.setStatus("PROCESSING");
        request.setResponse(null);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.responseFeedback(request));

        // then
        Assertions.assertEquals(INVALID_RESPONSE, exception.getMessage());
    }

    @Test
    void test_response_feedback_fail_when_invalid_status() {
        // given
        ResponseFeedbackRequest request = new ResponseFeedbackRequest();
        request.setId(1L);
        request.setStatus("meo");
        request.setResponse("la la");

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.responseFeedback(request));

        // then
        Assertions.assertEquals(INVALID_FEEDBACK_STATUS, exception.getMessage());
    }

    @Test
    void test_response_feedback_fail_when_feedback_id_is_null() {
        // given
        ResponseFeedbackRequest request = new ResponseFeedbackRequest();
        request.setId(null);
        request.setStatus("PROCESSING");
        request.setResponse("la la");

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.responseFeedback(request));

        // then
        Assertions.assertEquals(FEEDBACK_ID_IS_REQUIRED, exception.getMessage());
    }

    @Test
    void test_response_feedback_fail_when_feedback_is_not_found() {
        // given
        ResponseFeedbackRequest request = new ResponseFeedbackRequest();
        request.setId(1000000L);
        request.setStatus("PROCESSING");
        request.setResponse("la la");

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.responseFeedback(request));

        // then
        Assertions.assertEquals(INVALID_FEEDBACK_ID, exception.getMessage());
    }

    @Test
    void test_get_feedbacks_success() {
        // given
        FeedbacksRequest request = new FeedbacksRequest();
        request.setPageNumber(0);
        request.setPageSize(5);

        setManagerContext(438L, "0865390063");

        // when
        FeedbacksResponse response = underTest.getFeedbacks(request).getBody();

        // then
        Assertions.assertEquals(5, response.getFeedbackList().size());
    }

    @Test
    void test_accept_cv_success() {
        // given
        AcceptCVRequest request = new AcceptCVRequest();
        request.setRepairerId(555L);

        setManagerContext(438L, "0865390063");

        // when
        AcceptCVResponse response = underTest.acceptCV(request).getBody();

        // then
        Assertions.assertEquals(ACCEPT_CV_SUCCESS, response.getMessage());
    }

    @Test
    void test_accept_cv_fail_when_user_is_not_pending_repairer() {
        // given
        AcceptCVRequest request = new AcceptCVRequest();
        request.setRepairerId(36L);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.acceptCV(request));

        // then
        Assertions.assertEquals(INVALID_PENDING_REPAIRER, exception.getMessage());
    }

    @Test
    void test_get_repairer_detail_success() {
        // given
        GetRepairerDetailRequest request = new GetRepairerDetailRequest();
        request.setRepairerId(555L);

        setManagerContext(438L, "0865390063");

        // when
        GetRepairerDetailResponse response = underTest.getRepairerDetail(request).getBody();

        // then
        Assertions.assertNotNull(response);
    }

    @Test
    void test_get_repairer_detail_fail_when_repairer_not_found() {
        // given
        GetRepairerDetailRequest request = new GetRepairerDetailRequest();
        request.setRepairerId(1000000L);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getRepairerDetail(request));

        // then
        Assertions.assertEquals(REPAIRER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void test_get_repairer_detail_success_when_repairer_was_accepted() {
        // given
        GetRepairerDetailRequest request = new GetRepairerDetailRequest();
        request.setRepairerId(52L);

        setManagerContext(438L, "0865390063");

        // when
        GetRepairerDetailResponse response = underTest.getRepairerDetail(request).getBody();

        // then
        Assertions.assertNotNull(response);
    }

    @Test
    void test_get_repairer_detail_success_when_dob_is_null() {
        // given
        GetRepairerDetailRequest request = new GetRepairerDetailRequest();
        request.setRepairerId(555L);

        User user = userDAO.findById(555L).get();
        user.setDateOfBirth(null);

        setManagerContext(438L, "0865390063");

        // when
        GetRepairerDetailResponse response = underTest.getRepairerDetail(request).getBody();

        // then
        Assertions.assertNotNull(response);
    }

    @Test
    void test_search_categories_success() {
        // given
        SearchCategoriesRequest request = new SearchCategoriesRequest();
        request.setKeyword("điện");

        setManagerContext(438L, "0865390063");

        // when
        SearchCategoriesResponse response = underTest.searchCategories(request).getBody();

        // then
        Assertions.assertNotNull(response.getCategories());
    }

    @Test
    void test_search_categories_fail_when_keyword_is_null() {
        // given
        SearchCategoriesRequest request = new SearchCategoriesRequest();
        request.setKeyword(null);

        setManagerContext(438L, "0865390063");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.searchCategories(request));

        // then
        Assertions.assertEquals(INVALID_KEY_WORD, exception.getMessage());
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