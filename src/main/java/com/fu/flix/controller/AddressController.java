package com.fu.flix.controller;

import com.fu.flix.dto.request.CommuneRequest;
import com.fu.flix.dto.request.DistrictRequest;
import com.fu.flix.dto.response.CityResponse;
import com.fu.flix.dto.response.CommuneResponse;
import com.fu.flix.dto.response.DistrictResponse;
import com.fu.flix.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("api/v1/address")
public class AddressController {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("city")
    public ResponseEntity<CityResponse> getAllCities() {
        return addressService.getAllCities();
    }

    @GetMapping("district")
    public ResponseEntity<DistrictResponse> getDistrictsByCity(@RequestBody DistrictRequest request) {
        return addressService.getDistrictByCity(request);
    }

    @GetMapping("commune")
    public ResponseEntity<CommuneResponse> getCommunesByDistrict(@RequestBody CommuneRequest request) {
        return addressService.getCommunesByDistrict(request);
    }
}
