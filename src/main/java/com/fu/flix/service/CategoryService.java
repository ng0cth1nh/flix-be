package com.fu.flix.service;

import com.fu.flix.dto.ServiceDTO;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import org.springframework.http.ResponseEntity;

public interface CategoryService {
    ResponseEntity<ServiceResponse> getServicesByCategory(ServiceRequest request);

    ResponseEntity<SearchActiveServicesResponse> searchServices(SearchActiveServicesRequest request);

    ServiceDTO mapToServiceDTO(com.fu.flix.entity.Service service);

    ResponseEntity<SubServiceResponse> getSubServicesByServiceId(SubServiceRequest request);

    ResponseEntity<AccessoriesResponse> getAccessoriesByServiceId(AccessoriesRequest request);

    ResponseEntity<GetAllCategoriesResponse> getAllCategories(GetAllCategoriesRequest request);

    ResponseEntity<GetAllServicesResponse> getAllServices(GetAllServicesRequest request);
}
