package com.fu.flix.service.impl;

import com.fu.flix.dao.RepairRequestDAO;
import com.fu.flix.dto.request.CancelRequestingRepairRequest;
import com.fu.flix.dto.request.RequestingRepairRequest;
import com.fu.flix.dto.response.CancelRequestingRepairResponse;
import com.fu.flix.dto.response.RequestingRepairResponse;
import com.fu.flix.entity.RepairRequest;
import com.fu.flix.service.CustomerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static com.fu.flix.constant.Constant.CANCEL_REPAIR_REQUEST_SUCCESSFUL;
import static com.fu.flix.constant.Constant.CREATE_REPAIR_REQUEST_SUCCESSFUL;
import static com.fu.flix.constant.enums.Status.PENDING;

@RunWith(SpringRunner.class)
@SpringBootTest
class CustomerServiceImplTest {

    @Autowired
    CustomerService customerService;

    @Autowired
    RepairRequestDAO repairRequestDAO;

    //        @Test
    void test_create_fixing_request_response_success() {

        // given
        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(1L);
        request.setAddressId(7L);
        request.setExpectFixingDay("2022-06-20 13:00:00");
        request.setDescription("Thợ phải đẹp trai");
        request.setVoucherId(1L);
        request.setPaymentMethodId("C");

        setContextUsername("0865390037");

        // when
        ResponseEntity<RequestingRepairResponse> responseEntity = customerService.createFixingRequest(request);
        RequestingRepairResponse response = responseEntity.getBody();

        // then
        Assertions.assertEquals(CREATE_REPAIR_REQUEST_SUCCESSFUL, response.getMessage());
        Assertions.assertEquals(PENDING.name(), response.getStatus());
    }

    //    @Test
    void test_cancel_repair_request_success() {
        // given
        String requestCode = "87a4c2eb-5413-4237-82b6-9f261c3d1825";
        CancelRequestingRepairRequest request = new CancelRequestingRepairRequest();
        request.setRequestCode(requestCode);

        setContextUsername("0865390037");

        // when
        ResponseEntity<CancelRequestingRepairResponse> responseEntity = customerService.cancelFixingRequest(request);
        CancelRequestingRepairResponse response = responseEntity.getBody();

        RepairRequest repairRequest = repairRequestDAO.findByRequestCode(requestCode).get();

        // then
        Assertions.assertEquals("C", repairRequest.getStatusId());
        Assertions.assertEquals(CANCEL_REPAIR_REQUEST_SUCCESSFUL, response.getMessage());

    }


    //    @Test
    void test_create_row_on_repair_request_table() {

        // given
        Long serviceId = 1L;
        Long addressId = 7L;
        String description = "Thợ phải đẹp trai";
        String paymentMethodId = "C";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setAddressId(addressId);
        request.setExpectFixingDay("2022-06-20 13:00:00");
        request.setDescription(description);
        request.setVoucherId(1L);
        request.setPaymentMethodId(paymentMethodId);

        setContextUsername("0865390037");

        // when
        ResponseEntity<RequestingRepairResponse> responseEntity = customerService.createFixingRequest(request);
        RequestingRepairResponse response = responseEntity.getBody();

        Optional<RepairRequest> optional = repairRequestDAO.findByRequestCode(response.getRequestCode());
        RepairRequest repairRequest = optional.get();

        Assertions.assertEquals(36, repairRequest.getUserId());
        Assertions.assertEquals(serviceId, repairRequest.getServiceId());
        Assertions.assertEquals(addressId, repairRequest.getAddressId());
        Assertions.assertEquals(LocalDateTime.of(2022, 06, 20, 13, 0, 0),
                repairRequest.getExpectStartFixingAt());
        Assertions.assertEquals(description, repairRequest.getDescription());
        Assertions.assertEquals(paymentMethodId, repairRequest.getPaymentMethodId());
    }

    void setContextUsername(String phone) {
        String[] roles = {"ROLE_CUSTOMER"};
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(phone, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}