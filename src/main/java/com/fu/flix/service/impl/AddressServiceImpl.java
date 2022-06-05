package com.fu.flix.service.impl;

import com.fu.flix.dao.CityDAO;
import com.fu.flix.dto.CityDTO;
import com.fu.flix.dto.response.CityResponse;
import com.fu.flix.entity.City;
import com.fu.flix.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AddressServiceImpl implements AddressService {
    private final CityDAO cityDAO;

    public AddressServiceImpl(CityDAO cityDAO) {
        this.cityDAO = cityDAO;
    }

    @Override
    public ResponseEntity<CityResponse> getAllCities() {
        List<City> cities = cityDAO.findAll();
        List<CityDTO> cityDTOS = cities.stream()
                .map(city -> {
                    CityDTO cityDTO = new CityDTO();
                    cityDTO.setLabel(city.getName());
                    cityDTO.setValue(city.getId());
                    return cityDTO;
                }).collect(Collectors.toList());

        CityResponse response = new CityResponse();
        response.setCities(cityDTOS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
