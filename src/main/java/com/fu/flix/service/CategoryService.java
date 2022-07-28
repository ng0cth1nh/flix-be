package com.fu.flix.service;

import com.fu.flix.dto.ServiceDTO;
import com.fu.flix.dto.request.SearchActiveServicesRequest;
import com.fu.flix.dto.request.ServiceRequest;
import com.fu.flix.dto.request.ServiceResponse;
import com.fu.flix.dto.response.SearchActiveServicesResponse;
import org.springframework.http.ResponseEntity;

public interface CategoryService {
    ResponseEntity<ServiceResponse> getServicesByCategory(ServiceRequest request);

    ResponseEntity<SearchActiveServicesResponse> searchServices(SearchActiveServicesRequest request);

    ServiceDTO mapToServiceDTO(com.fu.flix.entity.Service service);
}
