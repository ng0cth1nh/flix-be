package com.fu.flix.service;

import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import org.springframework.http.ResponseEntity;

public interface CommonRepairerService {
    ResponseEntity<RequestingSuggestionResponse> getSuggestionRequestList(RequestingSuggestionRequest request);

    ResponseEntity<RequestingFilterResponse> getFilterRequestList(RequestingFilterRequest request);

    ResponseEntity<SearchSubServicesResponse> searchSubServicesByService(SearchSubServicesRequest request);

    ResponseEntity<SearchAccessoriesResponse> searchAccessoriesByService(SearchAccessoriesRequest request);

    ResponseEntity<RepairerProfileResponse> getRepairerProfile(RepairerProfileRequest request);
}
