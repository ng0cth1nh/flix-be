package com.fu.flix.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AccountService {

    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;

    ResponseEntity<SendRegisterOTPResponse> sendRegisterOTP(SendRegisterOTPRequest request) throws JsonProcessingException;

    ResponseEntity<CFRegisterCustomerResponse> confirmRegisterCustomer(CFRegisterCustomerRequest request) throws IOException;

    ResponseEntity<SendForgotPassOTPResponse> sendForgotPassOTP(SendForgotPassOTPRequest request) throws JsonProcessingException;

    ResponseEntity<CFForgotPassResponse> confirmForgotPassword(CFForgotPassRequest request);

    ResponseEntity<ResetPasswordResponse> resetPassword(ResetPasswordRequest request);

    ResponseEntity<CFRegisterRepairerResponse> confirmRegisterRepairer(CFRegisterRepairerRequest request) throws IOException;
}
