package com.fu.flix.service;

import com.fu.flix.dto.request.CommuneRequest;
import com.fu.flix.dto.request.DistrictRequest;
import com.fu.flix.dto.response.CityResponse;
import com.fu.flix.dto.response.CommuneResponse;
import com.fu.flix.dto.response.DistrictResponse;
import org.springframework.http.ResponseEntity;

public interface AddressService {
    ResponseEntity<CityResponse> getAllCities();

    ResponseEntity<DistrictResponse> getDistrictByCity(DistrictRequest request);

    ResponseEntity<CommuneResponse> getCommunesByDistrict(CommuneRequest request);
    String getAddressFormatted(Long addressId);
}
