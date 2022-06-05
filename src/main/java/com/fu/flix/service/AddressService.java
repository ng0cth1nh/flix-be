package com.fu.flix.service;

import com.fu.flix.dto.response.CityResponse;
import org.springframework.http.ResponseEntity;

public interface AddressService {
    ResponseEntity<CityResponse> getAllCities();
}
