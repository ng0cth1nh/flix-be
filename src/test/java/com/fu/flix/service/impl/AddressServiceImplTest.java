package com.fu.flix.service.impl;

import com.fu.flix.dto.CityDTO;
import com.fu.flix.dto.DistrictDTO;
import com.fu.flix.dto.request.DistrictRequest;
import com.fu.flix.dto.response.CityResponse;
import com.fu.flix.dto.response.DistrictResponse;
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

    @Test
    void test_get_districts_by_city() {
        // given
        DistrictRequest request = new DistrictRequest();
        request.setCityId("01");

        // when
        ResponseEntity<DistrictResponse> response = addressService.getDistrictByCity(request);
        List<DistrictDTO> districts = response.getBody().getDistricts();

        // then
        Assertions.assertEquals(30, districts.size());
    }
}