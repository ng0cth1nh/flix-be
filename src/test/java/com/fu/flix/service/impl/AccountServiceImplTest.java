package com.fu.flix.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.RegisterCustomerRequest;
import com.fu.flix.dto.request.RegisterRepairerRequest;
import com.fu.flix.dto.response.RegisterCustomerResponse;
import com.fu.flix.dto.response.RegisterRepairerResponse;
import com.fu.flix.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static com.fu.flix.constant.Constant.ACCOUNT_EXISTED;
import static com.fu.flix.constant.Constant.NEW_ACCOUNT_VALID;

@RunWith(SpringRunner.class)
@SpringBootTest
class AccountServiceImplTest {

    @Autowired
    AccountServiceImpl accountServiceImpl;

//    @Test
    void should_register_customer_successful() throws JsonProcessingException {
        // given
        String phone = "0865390031";
        RegisterCustomerRequest request = new RegisterCustomerRequest();
        request.setPhone(phone);
        request.setFullName("La La");
        request.setPassword("123456");

        // when
        ResponseEntity<RegisterCustomerResponse> response = accountServiceImpl.registerCustomer(request);

        // then
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(NEW_ACCOUNT_VALID, response.getBody().getMessage());
    }

    @Test
    void should_throw_error_when_register_customer_existed() {
        // given
        String phone = "0962706247";
        RegisterCustomerRequest request = new RegisterCustomerRequest();
        request.setPhone(phone);
        request.setFullName("La La");
        request.setPassword("123456");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> accountServiceImpl.registerCustomer(request));

        // then
        Assertions.assertEquals(ACCOUNT_EXISTED, exception.getMessage());
    }

//    @Test
    void should_register_repairer_successful() throws JsonProcessingException {
        // given
        String phone = "0865390031";
        RegisterRepairerRequest request = new RegisterRepairerRequest();
        request.setPhone(phone);
        request.setFullName("La La");
        request.setPassword("123456");

        // when
        ResponseEntity<RegisterRepairerResponse> response = accountServiceImpl.registerRepairer(request);

        // then
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(NEW_ACCOUNT_VALID, response.getBody().getMessage());
    }

    @Test
    void should_throw_error_when_register_repairer_existed() {
        // given
        String phone = "0962706247";
        RegisterRepairerRequest request = new RegisterRepairerRequest();
        request.setPhone(phone);
        request.setFullName("La La");
        request.setPassword("123456");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> accountServiceImpl.registerRepairer(request));

        // then
        Assertions.assertEquals(ACCOUNT_EXISTED, exception.getMessage());
    }
}