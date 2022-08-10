package com.fu.flix.controller;

import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.service.CommonRepairerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("api/v1/commonRepairer")
public class CommonRepairerController {
    private final CommonRepairerService commonRepairerService;

    public CommonRepairerController(CommonRepairerService commonRepairerService) {
        this.commonRepairerService = commonRepairerService;
    }

    @GetMapping("request/list/suggestion")
    public ResponseEntity<RequestingSuggestionResponse> getSuggestionRequestList(RequestingSuggestionRequest request) {
        return commonRepairerService.getSuggestionRequestList(request);
    }

    @GetMapping("request/list/filter")
    public ResponseEntity<RequestingFilterResponse> getFilterRequestList(RequestingFilterRequest request) {
        return commonRepairerService.getFilterRequestList(request);
    }

    @GetMapping("subServices")
    public ResponseEntity<SearchSubServicesResponse> searchSubServicesByService(SearchSubServicesRequest request) {
        return commonRepairerService.searchSubServicesByService(request);
    }

    @GetMapping("accessories")
    public ResponseEntity<SearchAccessoriesResponse> searchAccessoriesByService(SearchAccessoriesRequest request) {
        return commonRepairerService.searchAccessoriesByService(request);
    }

    @GetMapping("profile")
    public ResponseEntity<RepairerProfileResponse> getRepairerProfile(RepairerProfileRequest request) {
        return commonRepairerService.getRepairerProfile(request);
    }

    @PutMapping("profile")
    public ResponseEntity<UpdateRepairerResponse> updateRepairerProfile(@RequestBody UpdateRepairerRequest request) {
        return commonRepairerService.updateRepairerProfile(request);
    }
}
