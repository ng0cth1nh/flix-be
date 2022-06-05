package com.fu.flix.controller;

import com.fu.flix.dto.response.CityResponse;
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
    public ResponseEntity<CityResponse> registerCustomer() {
        return addressService.getAllCities();
    }
}
