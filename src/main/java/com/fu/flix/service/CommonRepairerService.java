package com.fu.flix.service;

import com.fu.flix.dto.request.RequestingFilterRequest;
import com.fu.flix.dto.request.RequestingSuggestionRequest;
import com.fu.flix.dto.request.SearchAccessoriesRequest;
import com.fu.flix.dto.request.SearchSubServicesRequest;
import com.fu.flix.dto.response.RequestingFilterResponse;
import com.fu.flix.dto.response.RequestingSuggestionResponse;
import com.fu.flix.dto.response.SearchAccessoriesResponse;
import com.fu.flix.dto.response.SearchSubServicesResponse;
import org.springframework.http.ResponseEntity;

public interface CommonRepairerService {
    ResponseEntity<RequestingSuggestionResponse> getSuggestionRequestList(RequestingSuggestionRequest request);

    ResponseEntity<RequestingFilterResponse> getFilterRequestList(RequestingFilterRequest request);

    ResponseEntity<SearchSubServicesResponse> searchSubServicesByService(SearchSubServicesRequest request);
    ResponseEntity<SearchAccessoriesResponse> searchAccessoriesByService(SearchAccessoriesRequest request);
}
