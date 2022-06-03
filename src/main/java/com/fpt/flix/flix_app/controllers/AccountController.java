package com.fpt.flix.flix_app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.flix.flix_app.models.requests.CFRegisterCustomerRequest;
import com.fpt.flix.flix_app.models.requests.RegisterCustomerRequest;
import com.fpt.flix.flix_app.models.requests.RegisterRepairerRequest;
import com.fpt.flix.flix_app.models.responses.CFRegisterCustomerResponse;
import com.fpt.flix.flix_app.models.responses.RegisterCustomerResponse;
import com.fpt.flix.flix_app.models.responses.RegisterRepairerResponse;
import com.fpt.flix.flix_app.services.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@Slf4j
@RequestMapping("api/v1/")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request,
                             HttpServletResponse response) throws IOException {
        accountService.refreshToken(request, response);
    }

    @PostMapping("register/customer")
    public ResponseEntity<RegisterCustomerResponse> registerCustomer(@RequestBody RegisterCustomerRequest request) throws JsonProcessingException {
        return accountService.registerCustomer(request);
    }

    @PostMapping("register/customer/confirm")
    public ResponseEntity<CFRegisterCustomerResponse> confirmRegisterCustomer(@RequestBody CFRegisterCustomerRequest request) {
        return accountService.confirmRegisterCustomer(request);
    }

    @PostMapping("register/repairer")
    public ResponseEntity<RegisterRepairerResponse> registerCustomer(@RequestBody RegisterRepairerRequest request) throws JsonProcessingException {
        return accountService.registerRepairer(request);
    }
}
