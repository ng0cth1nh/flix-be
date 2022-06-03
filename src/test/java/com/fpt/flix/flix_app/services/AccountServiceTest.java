package com.fpt.flix.flix_app.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.flix.flix_app.models.errors.GeneralException;
import com.fpt.flix.flix_app.models.requests.RegisterCustomerRequest;
import com.fpt.flix.flix_app.models.requests.RegisterRepairerRequest;
import com.fpt.flix.flix_app.models.responses.RegisterCustomerResponse;
import com.fpt.flix.flix_app.models.responses.RegisterRepairerResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static com.fpt.flix.flix_app.constants.Constant.ACCOUNT_EXISTED;
import static com.fpt.flix.flix_app.constants.Constant.NEW_ACCOUNT_VALID;

@RunWith(SpringRunner.class)
@SpringBootTest
class AccountServiceTest {

    @Autowired
    AccountService accountService;

    @Test
    void should_register_customer_successful() throws JsonProcessingException {
        // given
        String phone = "0865390031";
        RegisterCustomerRequest request = new RegisterCustomerRequest();
        request.setPhone(phone);
        request.setFirstName("Lan");
        request.setLastName("Mai");
        request.setPassword("123456");

        // when
        ResponseEntity<RegisterCustomerResponse> response = accountService.registerCustomer(request);

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
        request.setFirstName("Lan");
        request.setLastName("Mai");
        request.setPassword("123456");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> accountService.registerCustomer(request));

        // then
        Assertions.assertEquals(ACCOUNT_EXISTED, exception.getMessage());
    }

    @Test
    void should_register_repairer_successful() throws JsonProcessingException {
        // given
        String phone = "0865390031";
        RegisterRepairerRequest request = new RegisterRepairerRequest();
        request.setPhone(phone);
        request.setFirstName("Trung");
        request.setLastName("Dung");
        request.setPassword("123456");

        // when
        ResponseEntity<RegisterRepairerResponse> response = accountService.registerRepairer(request);

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
        request.setFirstName("Lan");
        request.setLastName("Mai");
        request.setPassword("123456");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> accountService.registerRepairer(request));

        // then
        Assertions.assertEquals(ACCOUNT_EXISTED, exception.getMessage());
    }
}