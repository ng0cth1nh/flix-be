package com.fu.flix.service;

import com.fu.flix.dto.request.CancelRequestingRepairRequest;
import com.fu.flix.dto.request.RequestingRepairRequest;
import com.fu.flix.dto.response.CancelRequestingRepairResponse;
import com.fu.flix.dto.response.RequestingRepairResponse;
import org.springframework.http.ResponseEntity;

public interface CustomerService {
    ResponseEntity<RequestingRepairResponse> createFixingRequest(RequestingRepairRequest request);
    ResponseEntity<CancelRequestingRepairResponse> cancelFixingRequest(CancelRequestingRepairRequest request);
}
