package com.fu.flix.service.impl;

import com.fu.flix.dto.ServiceDTO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.service.CategoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.List;

import static com.fu.flix.constant.Constant.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class CategoryServiceImplTest {

    @Autowired
    CategoryService underTest;

    @Test
    void getServicesByCategory_success() {
        // given
        ServiceRequest request = new ServiceRequest();
        request.setCategoryId(1L);

        // when
        ResponseEntity<ServiceResponse> responseEntity = underTest.getServicesByCategory(request);
        ServiceResponse serviceResponse = responseEntity.getBody();
        List<ServiceDTO> services = serviceResponse.getServices();

        // then
        Assertions.assertNotNull(services);
    }

    @Test
    void getServicesByCategory_fail_when_category_id_is_null() {
        // given
        ServiceRequest request = new ServiceRequest();
        request.setCategoryId(null);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getServicesByCategory(request));

        // then
        Assertions.assertEquals(INVALID_CATEGORY_ID, exception.getMessage());
    }

    @Test
    void getServicesByCategory_fail_when_category_id_is_0() {
        // given
        ServiceRequest request = new ServiceRequest();
        request.setCategoryId(0L);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getServicesByCategory(request));

        // then
        Assertions.assertEquals(INVALID_CATEGORY_ID, exception.getMessage());
    }

    @Test
    void test_search_service_success_when_key_word_is_nh() {
        // given
        SearchActiveServicesRequest request = new SearchActiveServicesRequest();
        request.setKeyword("nh");

        // when
        SearchActiveServicesResponse response = underTest.searchServices(request).getBody();

        // then
        Assertions.assertTrue(response.getServices().size() > 0);
    }

    @Test
    void test_search_service_success_when_key_word_contain_utf_8_character_and_white_space() {
        // given
        SearchActiveServicesRequest request = new SearchActiveServicesRequest();
        request.setKeyword("máy giặt");

        // when
        SearchActiveServicesResponse response = underTest.searchServices(request).getBody();

        // then
        Assertions.assertTrue(response.getServices().size() > 0);
    }

    @Test
    void test_search_service_success_when_key_word_is_123a() {
        // given
        SearchActiveServicesRequest request = new SearchActiveServicesRequest();
        request.setKeyword("123a");

        // when
        SearchActiveServicesResponse response = underTest.searchServices(request).getBody();

        // then
        Assertions.assertTrue(response.getServices().isEmpty());
    }

    @Test
    void test_get_sub_service_by_service_id_success() {
        // given
        SubServiceRequest request = new SubServiceRequest();
        request.setServiceId(1L);

        // when
        SubServiceResponse response = underTest.getSubServicesByServiceId(request).getBody();

        // then
        Assertions.assertNotNull(response.getSubServices());
    }

    @Test
    void test_get_sub_service_by_service_id_fail_when_service_id_is_null() {
        // given
        SubServiceRequest request = new SubServiceRequest();
        request.setServiceId(null);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getSubServicesByServiceId(request));

        // then
        Assertions.assertEquals(INVALID_SERVICE, exception.getMessage());
    }

    @Test
    void test_get_accessories_by_service_id_success() {
        // given
        AccessoriesRequest request = new AccessoriesRequest();
        request.setServiceId(1L);

        // when
        AccessoriesResponse response = underTest.getAccessoriesByServiceId(request).getBody();

        // then
        Assertions.assertNotNull(response.getAccessories());
    }

    @Test
    void test_get_accessories_by_service_id_fail_when_service_id_is_null() {
        // given
        AccessoriesRequest request = new AccessoriesRequest();
        request.setServiceId(null);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getAccessoriesByServiceId(request));

        // then
        Assertions.assertEquals(INVALID_SERVICE, exception.getMessage());
    }

    @Test
    void test_getAllCategories_success() {
        // given
        GetAllCategoriesRequest request = new GetAllCategoriesRequest();

        // when
        GetAllCategoriesResponse response = underTest.getAllCategories(request).getBody();

        // then
        Assertions.assertNotNull(response.getCategories());
    }

    @Test
    void test_getAllServices_success() {
        // given
        GetAllServicesRequest request = new GetAllServicesRequest();

        // when
        GetAllServicesResponse response = underTest.getAllServices(request).getBody();

        // then
        Assertions.assertNotNull(response.getServices());
    }
}