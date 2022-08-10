package com.fu.flix.service.impl;

import com.fu.flix.dto.CityDTO;
import com.fu.flix.dto.CommuneDTO;
import com.fu.flix.dto.DistrictDTO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.CommuneRequest;
import com.fu.flix.dto.request.DistrictRequest;
import com.fu.flix.dto.response.CityResponse;
import com.fu.flix.dto.response.CommuneResponse;
import com.fu.flix.dto.response.DistrictResponse;
import com.fu.flix.service.AddressService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.List;

import static com.fu.flix.constant.Constant.INVALID_CITY;
import static com.fu.flix.constant.Constant.INVALID_DISTRICT;


@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class AddressServiceImplTest {

    @Autowired
    AddressService underTest;

    @Test
    void test_get_all_cities() {
        // when
        ResponseEntity<CityResponse> response = underTest.getAllCities();
        List<CityDTO> cities = response.getBody().getCities();

        // then
        Assertions.assertEquals(63, cities.size());
    }

    @Test
    void test_get_districts_by_city_success() {
        // given
        DistrictRequest request = new DistrictRequest();
        request.setCityId("01");

        // when
        ResponseEntity<DistrictResponse> response = underTest.getDistrictByCity(request);
        List<DistrictDTO> districts = response.getBody().getDistricts();

        // then
        Assertions.assertEquals(30, districts.size());
    }

    @Test
    void test_get_districts_by_city_fail_when_city_id_is_empty() {
        // given
        DistrictRequest request = new DistrictRequest();
        request.setCityId("");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getDistrictByCity(request));

        // then
        Assertions.assertEquals(INVALID_CITY, exception.getMessage());
    }

    @Test
    void test_get_districts_by_city_fail_when_city_id_is_null() {
        // given
        DistrictRequest request = new DistrictRequest();
        request.setCityId(null);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getDistrictByCity(request));

        // then
        Assertions.assertEquals(INVALID_CITY, exception.getMessage());
    }

    @Test
    void test_get_districts_by_city_fail_when_city_id_is_abc() {
        // given
        DistrictRequest request = new DistrictRequest();
        request.setCityId(null);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getDistrictByCity(request));

        // then
        Assertions.assertEquals(INVALID_CITY, exception.getMessage());
    }

    @Test
    void test_get_districts_by_city_fail_when_city_id_is_a5() {
        // given
        DistrictRequest request = new DistrictRequest();
        request.setCityId(null);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getDistrictByCity(request));

        // then
        Assertions.assertEquals(INVALID_CITY, exception.getMessage());
    }

    @Test
    void test_get_districts_by_city_fail_when_city_id_is_0() {
        // given
        DistrictRequest request = new DistrictRequest();
        request.setCityId(null);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getDistrictByCity(request));

        // then
        Assertions.assertEquals(INVALID_CITY, exception.getMessage());
    }

    @Test
    void test_get_communes_by_district_success() {
        // given
        CommuneRequest request = new CommuneRequest();
        request.setDistrictId("001");

        // when
        ResponseEntity<CommuneResponse> response = underTest.getCommunesByDistrict(request);
        List<CommuneDTO> communes = response.getBody().getCommunes();

        // then
        Assertions.assertEquals(14, communes.size());
    }

    @Test
    void test_get_communes_by_district_fail_when_district_id_is_empty() {
        // given
        CommuneRequest request = new CommuneRequest();
        request.setDistrictId("");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getCommunesByDistrict(request));

        // then
        Assertions.assertEquals(INVALID_DISTRICT, exception.getMessage());
    }

    @Test
    void test_get_communes_by_district_fail_when_district_id_is_null() {
        // given
        CommuneRequest request = new CommuneRequest();
        request.setDistrictId(null);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getCommunesByDistrict(request));

        // then
        Assertions.assertEquals(INVALID_DISTRICT, exception.getMessage());
    }

    @Test
    void test_get_communes_by_district_fail_when_district_id_is_abc() {
        // given
        CommuneRequest request = new CommuneRequest();
        request.setDistrictId("abc");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getCommunesByDistrict(request));

        // then
        Assertions.assertEquals(INVALID_DISTRICT, exception.getMessage());
    }

    @Test
    void test_get_communes_by_district_fail_when_district_id_is_a05() {
        // given
        CommuneRequest request = new CommuneRequest();
        request.setDistrictId("a05");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getCommunesByDistrict(request));

        // then
        Assertions.assertEquals(INVALID_DISTRICT, exception.getMessage());
    }

    @Test
    void test_get_communes_by_district_fail_when_district_id_is_0() {
        // given
        CommuneRequest request = new CommuneRequest();
        request.setDistrictId("0");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getCommunesByDistrict(request));

        // then
        Assertions.assertEquals(INVALID_DISTRICT, exception.getMessage());
    }
}