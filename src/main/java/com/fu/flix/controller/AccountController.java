package com.fu.flix.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fu.flix.dto.request.CFRegisterCustomerRequest;
import com.fu.flix.dto.request.CFRegisterRepairerRequest;
import com.fu.flix.dto.request.RegisterCustomerRequest;
import com.fu.flix.dto.request.RegisterRepairerRequest;
import com.fu.flix.dto.response.CFRegisterCustomerResponse;
import com.fu.flix.dto.response.CFRegisterRepairerResponse;
import com.fu.flix.dto.response.RegisterCustomerResponse;
import com.fu.flix.dto.response.RegisterRepairerResponse;
import com.fu.flix.service.AccountService;
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
    public ResponseEntity<CFRegisterCustomerResponse> confirmRegisterCustomer(CFRegisterCustomerRequest request) {
        return accountService.confirmRegisterCustomer(request);
    }

    @PostMapping("register/repairer")
    public ResponseEntity<RegisterRepairerResponse> registerCustomer(@RequestBody RegisterRepairerRequest request) throws JsonProcessingException {
        return accountService.registerRepairer(request);
    }

    @PostMapping("register/repairer/confirm")
    public ResponseEntity<CFRegisterRepairerResponse> confirmRegisterRepairer(CFRegisterRepairerRequest request) {
        return accountService.confirmRegisterRepairer(request);
    }
}
