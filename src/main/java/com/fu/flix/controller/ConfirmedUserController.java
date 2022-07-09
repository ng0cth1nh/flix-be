package com.fu.flix.controller;

import com.fu.flix.dto.request.CommentRequest;
import com.fu.flix.dto.request.GetFixedServiceRequest;
import com.fu.flix.dto.request.GetInvoiceRequest;
import com.fu.flix.dto.response.CommentResponse;
import com.fu.flix.dto.response.GetFixedServiceResponse;
import com.fu.flix.dto.response.GetInvoiceResponse;
import com.fu.flix.service.ConfirmedUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("api/v1/confirmedUser")
public class ConfirmedUserController {
    private final ConfirmedUserService confirmedUserService;

    public ConfirmedUserController(ConfirmedUserService confirmedUserService) {
        this.confirmedUserService = confirmedUserService;
    }

    @PostMapping("comment")
    public ResponseEntity<CommentResponse> createComment(@RequestBody CommentRequest request) {
        return confirmedUserService.createComment(request);
    }

    @GetMapping("request/invoice")
    public ResponseEntity<GetInvoiceResponse> getInvoice(GetInvoiceRequest request) {
        return confirmedUserService.getInvoice(request);
    }

    @GetMapping("request/fixedService")
    public ResponseEntity<GetFixedServiceResponse> getFixedServices(GetFixedServiceRequest request) {
        return confirmedUserService.getFixedServices(request);
    }
}
