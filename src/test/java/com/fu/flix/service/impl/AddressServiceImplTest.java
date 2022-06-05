package com.fu.flix.service.impl;

import com.fu.flix.dto.CityDTO;
import com.fu.flix.dto.response.CityResponse;
import com.fu.flix.service.AddressService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
class AddressServiceImplTest {

    @Autowired
    AddressService addressService;

    @Test
    void test_get_all_cities() {
        // when
        ResponseEntity<CityResponse> response = addressService.getAllCities();
        List<CityDTO> cities = response.getBody().getCities();

        // then
        Assertions.assertEquals(63, cities.size());
    }
}