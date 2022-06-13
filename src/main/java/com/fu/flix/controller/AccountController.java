package com.fu.flix.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fu.flix.dto.request.CFRegisterRequest;
import com.fu.flix.dto.request.CheckUsernameRequest;
import com.fu.flix.dto.response.CFRegisterResponse;
import com.fu.flix.dto.response.CheckUsernameResponse;
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
    public ResponseEntity<CheckUsernameResponse> sendRegisterOTP(@RequestBody CheckUsernameRequest request) throws JsonProcessingException {
        return accountService.sendRegisterOTP(request);
    }

    @RequestMapping(value = "register/confirm", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public ResponseEntity<CFRegisterResponse> confirmRegisterCustomer(CFRegisterRequest request) throws IOException {
        return accountService.confirmRegister(request);
    }
}
