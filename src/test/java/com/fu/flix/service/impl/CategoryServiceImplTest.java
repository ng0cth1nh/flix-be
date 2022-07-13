package com.fu.flix.service.impl;

import com.fu.flix.dto.ServiceDTO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.SearchActiveServicesRequest;
import com.fu.flix.dto.request.ServiceRequest;
import com.fu.flix.dto.request.ServiceResponse;
import com.fu.flix.dto.response.SearchActiveServicesResponse;
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

import static com.fu.flix.constant.Constant.INVALID_CATEGORY_ID;
import static com.fu.flix.constant.Constant.INVALID_KEY_WORD;

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
        Assertions.assertEquals(4, services.size());
        Assertions.assertEquals("Tivi", services.get(0).getServiceName());
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
    void test_search_service_fail_when_key_word_is_empty() {
        // given
        SearchActiveServicesRequest request = new SearchActiveServicesRequest();
        request.setKeyword("");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.searchServices(request));

        // then
        Assertions.assertEquals(INVALID_KEY_WORD, exception.getMessage());
    }

    @Test
    void test_search_service_fail_when_key_word_is_null() {
        // given
        SearchActiveServicesRequest request = new SearchActiveServicesRequest();
        request.setKeyword(null);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.searchServices(request));

        // then
        Assertions.assertEquals(INVALID_KEY_WORD, exception.getMessage());
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
}