package com.fu.flix.controller;

import com.fu.flix.dto.request.CustomerPaymentUrlRequest;
import com.fu.flix.dto.request.RepairerDepositUrlRequest;
import com.fu.flix.dto.response.CustomerPaymentResponse;
import com.fu.flix.dto.response.CustomerPaymentUrlResponse;
import com.fu.flix.dto.response.RepairerDepositResponse;
import com.fu.flix.dto.response.RepairerDepositUrlResponse;
import com.fu.flix.service.VNPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;


@RestController
@Slf4j
@RequestMapping("api/v1")
public class VnPayController {
    private final VNPayService vnPayService;

    public VnPayController(VNPayService vnPayService) {
        this.vnPayService = vnPayService;
    }

    @PostMapping("customer/vnpay/payment/url")
    public ResponseEntity<CustomerPaymentUrlResponse> createCustomerPaymentUrl(
            @RequestBody CustomerPaymentUrlRequest customerPaymentUrlRequest,
            HttpServletRequest httpServletRequest
    ) {
        return vnPayService.createCustomerPaymentUrl(customerPaymentUrlRequest, httpServletRequest);
    }

    @GetMapping("payment/response")
    public ResponseEntity<CustomerPaymentResponse> responseCustomerPayment(@RequestParam Map<String, String> requestParams) throws IOException {
        return vnPayService.responseCustomerPayment(requestParams);
    }

    @PostMapping("repairer/vnpay/deposit/url")
    public ResponseEntity<RepairerDepositUrlResponse> createRepairerDepositUrl(
            @RequestBody RepairerDepositUrlRequest repairerDepositUrlRequest,
            HttpServletRequest httpServletRequest
    ) {
        return vnPayService.createRepairerDepositUrl(repairerDepositUrlRequest, httpServletRequest);
    }

    @GetMapping("deposit/response")
    public ResponseEntity<RepairerDepositResponse> responseRepairerDeposit(@RequestParam  Map<String, String> requestParams) {
        return vnPayService.responseRepairerDeposit(requestParams);
    }
}
