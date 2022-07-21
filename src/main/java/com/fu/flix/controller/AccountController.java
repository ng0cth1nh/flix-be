package com.fu.flix.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
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

    @PostMapping("register/sendOTP")
    public ResponseEntity<SendRegisterOTPResponse> sendRegisterOTP(@RequestBody SendRegisterOTPRequest request) throws JsonProcessingException {
        return accountService.sendRegisterOTP(request);
    }

    @RequestMapping(value = "register/customer/confirm", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public ResponseEntity<CFRegisterCustomerResponse> confirmRegisterCustomer(CFRegisterCustomerUserRequest request) throws IOException {
        return accountService.confirmRegisterCustomer(request);
    }

    @PostMapping("forgot/password/sendOTP")
    public ResponseEntity<SendForgotPassOTPResponse> sendForgotPassOTP(@RequestBody SendForgotPassOTPRequest request) throws JsonProcessingException {
        return accountService.sendForgotPassOTP(request);
    }

    @PostMapping("forgot/password/confirm")
    public ResponseEntity<CFForgotPassResponse> confirmForgotPassword(@RequestBody CFForgotPassRequest request) {
        return accountService.confirmForgotPassword(request);
    }

    @PutMapping("forgot/password/reset")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        return accountService.resetPassword(request);
    }

    @RequestMapping(value = "register/repairer/confirm", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public ResponseEntity<CFRegisterRepairerResponse> confirmRegisterRepairer(CFRegisterRepairerUserRequest request) throws IOException {
        return accountService.confirmRegisterRepairer(request);
    }
}
