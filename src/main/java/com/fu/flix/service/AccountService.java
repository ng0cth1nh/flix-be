package com.fu.flix.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.CFRegisterResponse;
import com.fu.flix.dto.response.CheckUsernameResponse;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AccountService {

    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;

    ResponseEntity<CheckUsernameResponse> sendRegisterOTP(CheckUsernameRequest request) throws JsonProcessingException;

    ResponseEntity<CFRegisterResponse> confirmRegister(CFRegisterRequest request) throws IOException;
}
