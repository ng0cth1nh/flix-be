package com.fu.flix.service;

import com.fu.flix.dto.request.CustomerPaymentUrlRequest;
import com.fu.flix.dto.response.CustomerPaymentResponse;
import com.fu.flix.dto.response.CustomerPaymentUrlResponse;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface VNPayService {
    ResponseEntity<CustomerPaymentUrlResponse> createCustomerPaymentUrl(CustomerPaymentUrlRequest customerPaymentUrlRequest,
                                                                        HttpServletRequest httpServletRequest) throws UnsupportedEncodingException;

    ResponseEntity<CustomerPaymentResponse> responseCustomerPayment(Map<String, String> requestParams) throws IOException;
}
