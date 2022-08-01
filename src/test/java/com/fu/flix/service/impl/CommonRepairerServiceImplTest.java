package com.fu.flix.service.impl;

import com.fu.flix.dao.UserDAO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.dto.security.UserPrincipal;
import com.fu.flix.entity.User;
import com.fu.flix.service.CommonRepairerService;
import com.fu.flix.service.CustomerService;
import com.fu.flix.util.DateFormatUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.fu.flix.constant.Constant.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class CommonRepairerServiceImplTest {
    @Autowired
    CommonRepairerService underTest;
    String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    @Autowired
    UserDAO userDAO;
    @Autowired
    CustomerService customerService;

    @Test
    void getSuggestionRequestList_success_when_type_is_SUGGESTED() {
        // given
        RequestingSuggestionRequest request = new RequestingSuggestionRequest();
        request.setType("SUGGESTED");

        // when
        createFixingRequest(36L, "0865390037");
        setRepairerContext(52L, "0865390052");
        RequestingSuggestionResponse response = underTest.getSuggestionRequestList(request).getBody();

        // then
        Assertions.assertNotNull(response.getRequestList());
    }

    @Test
    void getSuggestionRequestList_success_when_type_is_INTERESTED() {
        // given
        RequestingSuggestionRequest request = new RequestingSuggestionRequest();
        request.setType("INTERESTED");

        // when
        createFixingRequest(36L, "0865390037");
        setRepairerContext(52L, "0865390052");
        RequestingSuggestionResponse response = underTest.getSuggestionRequestList(request).getBody();

        // then
        Assertions.assertNotNull(response.getRequestList());
    }

    @Test
    void getSuggestionRequestList_fail_when_type_is_empty() {
        // given
        RequestingSuggestionRequest request = new RequestingSuggestionRequest();
        request.setType("");

        // when
        setRepairerContext(52L, "0865390052");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getSuggestionRequestList(request).getBody());

        // then
        Assertions.assertEquals(INVALID_REPAIRER_SUGGESTION_TYPE, exception.getMessage());
    }

    @Test
    void getSuggestionRequestList_fail_when_type_is_null() {
        // given
        RequestingSuggestionRequest request = new RequestingSuggestionRequest();
        request.setType(null);

        // when
        setRepairerContext(52L, "0865390052");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getSuggestionRequestList(request).getBody());

        // then
        Assertions.assertEquals(INVALID_REPAIRER_SUGGESTION_TYPE, exception.getMessage());
    }

    @Test
    void getSuggestionRequestList_fail_when_type_is_wrong() {
        // given
        RequestingSuggestionRequest request = new RequestingSuggestionRequest();
        request.setType("abc");

        // when
        setRepairerContext(52L, "0865390052");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getSuggestionRequestList(request).getBody());

        // then
        Assertions.assertEquals(INVALID_REPAIRER_SUGGESTION_TYPE, exception.getMessage());
    }

    private String createFixingRequest(Long userId, String phone) {
        setCustomerContext(userId, phone);
        Long serviceId = 1L;
        Long addressId = 7L;
        String expectFixingDay = DateFormatUtil.toString(LocalDateTime.now().plusDays(2L), DATE_TIME_PATTERN);
        String description = "Thợ phải đẹp trai nha";
        Long voucherId = 1L;
        String paymentMethodId = "C";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setVoucherId(voucherId);
        request.setDescription(description);
        request.setExpectFixingDay(expectFixingDay);
        request.setAddressId(addressId);
        request.setPaymentMethodId(paymentMethodId);

        RequestingRepairResponse response = customerService.createFixingRequest(request).getBody();
        return response.getRequestCode();
    }

    @Test
    public void test_get_filter_request_list_success_when_location_type_is_COMMUNE() {
        // given
        List<Long> serviceIds = new ArrayList<>();
        serviceIds.add(1L);
        serviceIds.add(2L);
        serviceIds.add(3L);
        serviceIds.add(4L);

        RequestingFilterRequest request = new RequestingFilterRequest();
        request.setServiceIds(serviceIds);
        request.setLocationType("COMMUNE");
        request.setLocationId("00001");
        request.setStartDate("20-06-2022");
        request.setEndDate("28-07-2022");

        createFixingRequest(36L, "0865390037");

        // when
        setRepairerContext(52L, "0865390057");
        RequestingFilterResponse response = underTest.getFilterRequestList(request).getBody();

        // then
        Assertions.assertNotNull(response.getRequestList());
    }

    @Test
    public void test_get_filter_request_list_fail_when_list_service_is_null() {
        // given
        RequestingFilterRequest request = new RequestingFilterRequest();
        request.setServiceIds(null);
        request.setLocationType("COMMUNE");
        request.setLocationId("00001");
        request.setStartDate("20-06-2022");
        request.setEndDate("28-07-2022");

        createFixingRequest(36L, "0865390037");

        // when
        setRepairerContext(52L, "0865390057");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getFilterRequestList(request));

        // then
        Assertions.assertEquals(SERVICE_IDS_ARE_REQUIRED, exception.getMessage());
    }

    @Test
    public void test_get_filter_request_list_success_when_location_id_is_null() {
        // given
        List<Long> serviceIds = new ArrayList<>();
        serviceIds.add(1L);
        serviceIds.add(2L);
        serviceIds.add(3L);
        serviceIds.add(4L);

        RequestingFilterRequest request = new RequestingFilterRequest();
        request.setServiceIds(serviceIds);
        request.setLocationType("COMMUNE");
        request.setLocationId(null);
        request.setStartDate("20-06-2022");
        request.setEndDate("28-07-2022");

        createFixingRequest(36L, "0865390037");

        // when
        setRepairerContext(52L, "0865390057");
        RequestingFilterResponse response = underTest.getFilterRequestList(request).getBody();

        // then
        Assertions.assertNotNull(response.getRequestList());
    }

    @Test
    public void test_get_filter_request_list_fail_when_location_type_is_null() {
        // given
        List<Long> serviceIds = new ArrayList<>();
        serviceIds.add(1L);
        serviceIds.add(2L);
        serviceIds.add(3L);
        serviceIds.add(4L);

        RequestingFilterRequest request = new RequestingFilterRequest();
        request.setServiceIds(serviceIds);
        request.setLocationType(null);
        request.setLocationId("00001");
        request.setStartDate("20-06-2022");
        request.setEndDate("28-07-2022");

        createFixingRequest(36L, "0865390037");

        // when
        setRepairerContext(52L, "0865390057");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getFilterRequestList(request));

        // then
        Assertions.assertEquals(INVALID_LOCATION_TYPE, exception.getMessage());
    }

    @Test
    public void test_get_filter_request_list_success_when_location_type_is_CITY() {
        // given
        List<Long> serviceIds = new ArrayList<>();
        serviceIds.add(1L);
        serviceIds.add(2L);
        serviceIds.add(3L);
        serviceIds.add(4L);

        RequestingFilterRequest request = new RequestingFilterRequest();
        request.setServiceIds(serviceIds);
        request.setLocationType("CITY");
        request.setLocationId("00001");
        request.setStartDate("20-06-2022");
        request.setEndDate("28-07-2022");

        createFixingRequest(36L, "0865390037");

        // when
        setRepairerContext(52L, "0865390057");
        RequestingFilterResponse response = underTest.getFilterRequestList(request).getBody();

        // then
        Assertions.assertNotNull(response.getRequestList());
    }

    @Test
    public void test_get_filter_request_list_success_when_location_type_is_DISTRICT() {
        // given
        List<Long> serviceIds = new ArrayList<>();
        serviceIds.add(1L);
        serviceIds.add(2L);
        serviceIds.add(3L);
        serviceIds.add(4L);

        RequestingFilterRequest request = new RequestingFilterRequest();
        request.setServiceIds(serviceIds);
        request.setLocationType("DISTRICT");
        request.setLocationId("00001");
        request.setStartDate("20-06-2022");
        request.setEndDate("28-07-2022");

        createFixingRequest(36L, "0865390037");

        // when
        setRepairerContext(52L, "0865390057");
        RequestingFilterResponse response = underTest.getFilterRequestList(request).getBody();

        // then
        Assertions.assertNotNull(response.getRequestList());
    }

    @Test
    public void test_get_filter_request_list_fail_when_start_date_is_null() {
        // given
        List<Long> serviceIds = new ArrayList<>();
        serviceIds.add(1L);
        serviceIds.add(2L);
        serviceIds.add(3L);
        serviceIds.add(4L);

        RequestingFilterRequest request = new RequestingFilterRequest();
        request.setServiceIds(serviceIds);
        request.setLocationType("DISTRICT");
        request.setLocationId("00001");
        request.setStartDate(null);
        request.setEndDate("28-07-2022");

        createFixingRequest(36L, "0865390037");

        // when
        setRepairerContext(52L, "0865390057");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getFilterRequestList(request));

        // then
        Assertions.assertEquals(START_DATE_AND_END_DATE_ARE_REQUIRED, exception.getMessage());
    }

    @Test
    public void test_get_filter_request_list_fail_when_start_date_is_wrong_format() {
        // given
        List<Long> serviceIds = new ArrayList<>();
        serviceIds.add(1L);
        serviceIds.add(2L);
        serviceIds.add(3L);
        serviceIds.add(4L);

        RequestingFilterRequest request = new RequestingFilterRequest();
        request.setServiceIds(serviceIds);
        request.setLocationType("DISTRICT");
        request.setLocationId("00001");
        request.setStartDate("aaa");
        request.setEndDate("28-07-2022");

        createFixingRequest(36L, "0865390037");

        // when
        setRepairerContext(52L, "0865390057");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getFilterRequestList(request));

        // then
        Assertions.assertEquals(WRONG_LOCAL_DATE_FORMAT, exception.getMessage());
    }

    @Test
    public void test_search_sub_services_by_service_success() {
        // given
        SearchSubServicesRequest request = new SearchSubServicesRequest();
        request.setKeyword("bo");
        request.setServiceId(1L);

        setRepairerContext(52L, "0865390057");

        // when
        SearchSubServicesResponse response = underTest.searchSubServicesByService(request).getBody();

        // then
        Assertions.assertNotNull(response.getSubServices());
    }

    @Test
    public void test_search_sub_services_by_service_fail_when_keyword_is_null() {
        // given
        SearchSubServicesRequest request = new SearchSubServicesRequest();
        request.setKeyword(null);
        request.setServiceId(1L);

        setRepairerContext(52L, "0865390057");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.searchSubServicesByService(request));

        // then
        Assertions.assertEquals(INVALID_KEY_WORD, exception.getMessage());
    }

    @Test
    public void test_search_sub_services_by_service_fail_when_service_id_is_null() {
        // given
        SearchSubServicesRequest request = new SearchSubServicesRequest();
        request.setKeyword("bo");
        request.setServiceId(null);

        setRepairerContext(52L, "0865390057");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.searchSubServicesByService(request));

        // then
        Assertions.assertEquals(SERVICE_ID_IS_REQUIRED, exception.getMessage());
    }

    @Test
    public void test_search_accessory_by_service_success() {
        // given
        SearchAccessoriesRequest request = new SearchAccessoriesRequest();
        request.setKeyword("fake");
        request.setServiceId(1L);

        setRepairerContext(52L, "0865390057");

        // when
        SearchAccessoriesResponse response = underTest.searchAccessoriesByService(request).getBody();

        // then
        Assertions.assertNotNull(response.getAccessories());
    }

    @Test
    public void test_search_accessory_by_service_fail_when_keyword_is_null() {
        // given
        SearchAccessoriesRequest request = new SearchAccessoriesRequest();
        request.setKeyword(null);
        request.setServiceId(1L);

        setRepairerContext(52L, "0865390057");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.searchAccessoriesByService(request));

        // then
        Assertions.assertEquals(INVALID_KEY_WORD, exception.getMessage());
    }

    @Test
    public void test_search_accessory_by_service_fail_when_service_id_is_null() {
        // given
        SearchAccessoriesRequest request = new SearchAccessoriesRequest();
        request.setKeyword("fake");
        request.setServiceId(null);

        setRepairerContext(52L, "0865390057");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.searchAccessoriesByService(request));

        // then
        Assertions.assertEquals(SERVICE_ID_IS_REQUIRED, exception.getMessage());
    }

    @Test
    void test_get_repairer_profile_success() {
        // given
        RepairerProfileRequest request = new RepairerProfileRequest();
        setRepairerContext(52L, "0865390057");

        // when
        RepairerProfileResponse response = underTest.getRepairerProfile(request).getBody();

        // then
        Assertions.assertNotNull(response);
    }

    @Test
    void test_get_repairer_profile_success_when_dob_is_null() {
        // given
        RepairerProfileRequest request = new RepairerProfileRequest();
        setRepairerContext(52L, "0865390057");

        User user = userDAO.findById(52L).get();
        user.setDateOfBirth(LocalDate.now().minusYears(20L));

        // when
        RepairerProfileResponse response = underTest.getRepairerProfile(request).getBody();

        // then
        Assertions.assertNotNull(response);
    }

    @Test
    void test_updateRepairerProfile_success() {
        // given
        UpdateRepairerRequest request = new UpdateRepairerRequest();
        List<Long> registerServices = new ArrayList<>();
        registerServices.add(1L);
        request.setEmail("thongu@gmail.com");
        request.setExperienceDescription("Tài ba nhất cái đất Thạch Thất 2");
        request.setCommuneId("00001");
        request.setStreetAddress("Trường Ép Tao Dê 2");
        request.setRegisterServices(registerServices);

        setRepairerContext(52L, "0865390057");

        // when
        UpdateRepairerResponse response = underTest.updateRepairerProfile(request).getBody();

        // then
        Assertions.assertEquals(UPDATE_REPAIRER_PROFILE_SUCCESS, response.getMessage());
    }

    @Test
    void test_updateRepairerProfile_success_when_address_is_change() {
        // given
        UpdateRepairerRequest request = new UpdateRepairerRequest();
        List<Long> registerServices = new ArrayList<>();
        registerServices.add(1L);
        request.setEmail("thongu@gmail.com");
        request.setExperienceDescription("Tài ba nhất cái đất Thạch Thất 2");
        request.setCommuneId("00004");
        request.setStreetAddress("Trường Ép Tao Dê 2");
        request.setRegisterServices(registerServices);

        setRepairerContext(52L, "0865390057");

        // when
        UpdateRepairerResponse response = underTest.updateRepairerProfile(request).getBody();

        // then
        Assertions.assertEquals(UPDATE_REPAIRER_PROFILE_SUCCESS, response.getMessage());
    }

    @Test
    void test_updateRepairerProfile_fail_when_invalid_email() {
        // given
        UpdateRepairerRequest request = new UpdateRepairerRequest();
        request.setEmail("123");
        request.setExperienceDescription("Tài ba nhất cái đất Thạch Thất 2");
        request.setCommuneId("00004");
        request.setStreetAddress("Trường Ép Tao Dê 2");

        setRepairerContext(52L, "0865390057");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.updateRepairerProfile(request));

        // then
        Assertions.assertEquals(INVALID_EMAIL, exception.getMessage());
    }

    @Test
    void test_updateRepairerProfile_fail_when_invalid_street_address() {
        // given
        UpdateRepairerRequest request = new UpdateRepairerRequest();
        request.setEmail("dung@gmail.com");
        request.setExperienceDescription("Tài ba nhất cái đất Thạch Thất 2");
        request.setCommuneId("00004");
        request.setStreetAddress(null);

        setRepairerContext(52L, "0865390057");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.updateRepairerProfile(request));

        // then
        Assertions.assertEquals(INVALID_STREET_ADDRESS, exception.getMessage());
    }

    @Test
    void test_updateRepairerProfile_fail_when_invalid_experience_description() {
        // given
        UpdateRepairerRequest request = new UpdateRepairerRequest();
        request.setEmail("dung@gmail.com");
        request.setExperienceDescription(null);
        request.setCommuneId("00004");
        request.setStreetAddress("la la");

        setRepairerContext(52L, "0865390057");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.updateRepairerProfile(request));

        // then
        Assertions.assertEquals(INVALID_EXPERIENCE_DESCRIPTION, exception.getMessage());
    }

    @Test
    void test_updateRepairerProfile_fail_when_invalid_commune() {
        // given
        UpdateRepairerRequest request = new UpdateRepairerRequest();
        request.setEmail("dung@gmail.com");
        request.setExperienceDescription("lal  al la");
        request.setCommuneId("01");
        request.setStreetAddress("la la");

        setRepairerContext(52L, "0865390057");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.updateRepairerProfile(request));

        // then
        Assertions.assertEquals(INVALID_COMMUNE, exception.getMessage());
    }

    @Test
    void test_updateRepairerProfile_fail_when_commune_is_null() {
        // given
        UpdateRepairerRequest request = new UpdateRepairerRequest();
        request.setEmail("dung@gmail.com");
        request.setExperienceDescription("lal  al la");
        request.setCommuneId(null);
        request.setStreetAddress("la la");

        setRepairerContext(52L, "0865390057");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.updateRepairerProfile(request));

        // then
        Assertions.assertEquals(INVALID_COMMUNE, exception.getMessage());
    }

    void setRepairerContext(Long id, String phone) {
        String[] roles = {"ROLE_REPAIRER"};
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(new UserPrincipal(id, phone, roles), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    void setCustomerContext(Long id, String phone) {
        String[] roles = {"ROLE_CUSTOMER"};
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(new UserPrincipal(id, phone, roles), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}