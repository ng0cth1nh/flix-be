package com.fu.flix.service;

import com.fu.flix.dto.request.RequestingRepairRequest;
import com.fu.flix.dto.response.RequestingRepairResponse;
import org.springframework.http.ResponseEntity;

public interface CustomerService {
    ResponseEntity<RequestingRepairResponse> createFixingRequest(RequestingRepairRequest request);
}
