package com.fu.flix.service.impl;

import com.fu.flix.dto.ServiceDTO;
import com.fu.flix.dto.request.ServiceRequest;
import com.fu.flix.dto.request.ServiceResponse;
import com.fu.flix.service.CategoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
class CategoryServiceImplTest {

    @Autowired
    CategoryService categoryService;

    @Test
    void getServicesByCategory() {
        // given
        ServiceRequest request = new ServiceRequest();
        request.setCategoryId(1L);

        // when
        ResponseEntity<ServiceResponse> responseEntity = categoryService.getServicesByCategory(request);
        ServiceResponse serviceResponse = responseEntity.getBody();
        List<ServiceDTO> services = serviceResponse.getServices();

        // then
        Assertions.assertEquals(4, services.size());
        Assertions.assertEquals("Tivi", services.get(0).getServiceName());
    }
}