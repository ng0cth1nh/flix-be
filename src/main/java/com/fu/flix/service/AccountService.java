package com.fu.flix.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fu.flix.dto.request.CFRegisterCustomerRequest;
import com.fu.flix.dto.request.CFRegisterRepairerRequest;
import com.fu.flix.dto.request.RegisterCustomerRequest;
import com.fu.flix.dto.request.RegisterRepairerRequest;
import com.fu.flix.dto.response.CFRegisterCustomerResponse;
import com.fu.flix.dto.response.CFRegisterRepairerResponse;
import com.fu.flix.dto.response.RegisterCustomerResponse;
import com.fu.flix.dto.response.RegisterRepairerResponse;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AccountService {

    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;

    ResponseEntity<RegisterCustomerResponse> registerCustomer(RegisterCustomerRequest request) throws JsonProcessingException;

    ResponseEntity<RegisterRepairerResponse> registerRepairer(RegisterRepairerRequest request) throws JsonProcessingException;

    public ResponseEntity<CFRegisterCustomerResponse> confirmRegisterCustomer(CFRegisterCustomerRequest request);

    ResponseEntity<CFRegisterRepairerResponse> confirmRegisterRepairer(CFRegisterRepairerRequest request);
}
