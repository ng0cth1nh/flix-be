package com.fu.flix.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.entity.Repairer;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface AccountService {

    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;

    ResponseEntity<SendRegisterOTPResponse> sendRegisterOTP(SendRegisterOTPRequest request) throws JsonProcessingException;

    ResponseEntity<CFRegisterCustomerResponse> confirmRegisterCustomer(CFRegisterCustomerRequest request) throws IOException;

    ResponseEntity<SendForgotPassOTPResponse> sendForgotPassOTP(SendForgotPassOTPRequest request) throws JsonProcessingException;

    ResponseEntity<CFForgotPassResponse> confirmForgotPassword(CFForgotPassRequest request);

    ResponseEntity<ResetPasswordResponse> resetPassword(ResetPasswordRequest request);

    ResponseEntity<CFRegisterRepairerResponse> confirmRegisterRepairer(CFRegisterRepairerRequest request) throws IOException;

    ResponseEntity<LoginResponse> login(LoginRequest request);

    boolean isInvalidRegisterServices(List<Long> registerServices);

    void updateServicesToRepairer(List<com.fu.flix.entity.Service> services, Repairer repairer);
}
