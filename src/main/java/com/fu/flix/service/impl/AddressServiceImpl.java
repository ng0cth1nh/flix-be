package com.fu.flix.service.impl;

import com.fu.flix.dao.CityDAO;
import com.fu.flix.dao.CommuneDAO;
import com.fu.flix.dao.DistrictDAO;
import com.fu.flix.dto.CityDTO;
import com.fu.flix.dto.CommuneDTO;
import com.fu.flix.dto.DistrictDTO;
import com.fu.flix.dto.request.CommuneRequest;
import com.fu.flix.dto.request.DistrictRequest;
import com.fu.flix.dto.response.CityResponse;
import com.fu.flix.dto.response.CommuneResponse;
import com.fu.flix.dto.response.DistrictResponse;
import com.fu.flix.entity.City;
import com.fu.flix.entity.Commune;
import com.fu.flix.entity.District;
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
    private final DistrictDAO districtDAO;
    private final CommuneDAO communeDAO;

    public AddressServiceImpl(CityDAO cityDAO,
                              DistrictDAO districtDAO,
                              CommuneDAO communeDAO) {
        this.cityDAO = cityDAO;
        this.districtDAO = districtDAO;
        this.communeDAO = communeDAO;
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

    @Override
    public ResponseEntity<DistrictResponse> getDistrictByCity(DistrictRequest request) {
        List<District> districts = districtDAO.findByCityId(request.getCityId());
        List<DistrictDTO> districtDTOS = districts.stream()
                .map(district -> {
                    DistrictDTO districtDTO = new DistrictDTO();
                    districtDTO.setLabel(district.getName());
                    districtDTO.setValue(district.getId());
                    return districtDTO;
                }).collect(Collectors.toList());

        DistrictResponse response = new DistrictResponse();
        response.setDistricts(districtDTOS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CommuneResponse> getCommunesByDistrict(CommuneRequest request) {
        List<Commune> communes = communeDAO.findByDistrictId(request.getDistrictId());
        List<CommuneDTO> communeDTOS = communes.stream()
                .map(commune -> {
                    CommuneDTO communeDTO = new CommuneDTO();
                    communeDTO.setLabel(commune.getName());
                    communeDTO.setValue(commune.getId());
                    return communeDTO;
                }).collect(Collectors.toList());

        CommuneResponse response = new CommuneResponse();
        response.setCommunes(communeDTOS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
