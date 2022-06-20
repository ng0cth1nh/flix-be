package com.fu.flix.service;

import com.fu.flix.dto.request.SearchServicesRequest;
import com.fu.flix.dto.request.ServiceRequest;
import com.fu.flix.dto.request.ServiceResponse;
import com.fu.flix.dto.response.SearchServicesResponse;
import org.springframework.http.ResponseEntity;

public interface CategoryService {
    ResponseEntity<ServiceResponse> getServicesByCategory(ServiceRequest request);

    ResponseEntity<SearchServicesResponse> searchServices(SearchServicesRequest request);
}
