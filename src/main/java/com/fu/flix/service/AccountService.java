package com.fu.flix.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.CFForgotPassResponse;
import com.fu.flix.dto.response.CFRegisterResponse;
import com.fu.flix.dto.response.SendForgotPassOTPResponse;
import com.fu.flix.dto.response.SendRegisterOTPResponse;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AccountService {

    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;

    ResponseEntity<SendRegisterOTPResponse> sendRegisterOTP(SendRegisterOTPRequest request) throws JsonProcessingException;

    ResponseEntity<CFRegisterResponse> confirmRegister(CFRegisterRequest request) throws IOException;

    ResponseEntity<SendForgotPassOTPResponse> sendForgotPassOTP(SendForgotPassOTPRequest request) throws JsonProcessingException;

    ResponseEntity<CFForgotPassResponse> confirmForgotPassword(CFForgotPassRequest request);
}
