package com.fu.flix.service;

import com.fu.flix.dto.request.CustomerPaymentUrlRequest;
import com.fu.flix.dto.request.RepairerDepositUrlRequest;
import com.fu.flix.dto.response.CustomerPaymentResponse;
import com.fu.flix.dto.response.CustomerPaymentUrlResponse;
import com.fu.flix.dto.response.RepairerDepositResponse;
import com.fu.flix.dto.response.RepairerDepositUrlResponse;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

public interface VNPayService {
    ResponseEntity<CustomerPaymentUrlResponse> createCustomerPaymentUrl(CustomerPaymentUrlRequest customerPaymentUrlRequest,
                                                                        HttpServletRequest httpServletRequest);

    ResponseEntity<CustomerPaymentResponse> responseCustomerPayment(Map<String, String> requestParams) throws IOException;

    ResponseEntity<RepairerDepositUrlResponse> createRepairerDepositUrl(RepairerDepositUrlRequest repairerDepositUrlRequest,
                                                                        HttpServletRequest httpServletRequest);

    ResponseEntity<RepairerDepositResponse> responseRepairerDeposit(Map<String, String> requestParams) throws IOException;

    String hmacSHA512(final String key, final String data);
}
