package com.fu.flix.service.impl;

import com.fu.flix.dto.ServiceDTO;
import com.fu.flix.dto.request.ServiceRequest;
import com.fu.flix.dto.request.ServiceResponse;
import com.fu.flix.service.MajorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class MajorServiceImplTest {

    @Autowired
    MajorService majorService;

    @Test
    void getServicesByMajor() {
        // given
        ServiceRequest request = new ServiceRequest();
        request.setMajorId(1L);

        // when
        ResponseEntity<ServiceResponse> responseEntity = majorService.getServicesByMajor(request);
        ServiceResponse serviceResponse = responseEntity.getBody();
        List<ServiceDTO> services = serviceResponse.getServices();

        // then
        Assertions.assertEquals(4, services.size());
        Assertions.assertEquals("Tivi", services.get(0).getServiceName());
    }
}