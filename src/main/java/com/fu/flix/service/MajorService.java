package com.fu.flix.service;

import com.fu.flix.dto.request.ServiceRequest;
import com.fu.flix.dto.request.ServiceResponse;
import org.springframework.http.ResponseEntity;

public interface MajorService {
    ResponseEntity<ServiceResponse> getServicesByMajor(ServiceRequest request);
}
