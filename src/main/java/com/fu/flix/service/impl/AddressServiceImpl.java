package com.fu.flix.service.impl;

import com.fu.flix.dao.*;
import com.fu.flix.dto.CityDTO;
import com.fu.flix.dto.CommuneDTO;
import com.fu.flix.dto.DistrictDTO;
import com.fu.flix.dto.MainAddressDTO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.CommuneRequest;
import com.fu.flix.dto.request.DistrictRequest;
import com.fu.flix.dto.response.CityResponse;
import com.fu.flix.dto.response.CommuneResponse;
import com.fu.flix.dto.response.DistrictResponse;
import com.fu.flix.entity.*;
import com.fu.flix.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fu.flix.constant.Constant.*;

@Service
@Slf4j
public class AddressServiceImpl implements AddressService {
    private final CityDAO cityDAO;
    private final DistrictDAO districtDAO;
    private final CommuneDAO communeDAO;
    private final UserAddressDAO userAddressDAO;
    private final UserDAO userDAO;
    private final String COMMA = ", ";

    public AddressServiceImpl(CityDAO cityDAO,
                              DistrictDAO districtDAO,
                              CommuneDAO communeDAO,
                              UserAddressDAO userAddressDAO,
                              UserDAO userDAO) {
        this.cityDAO = cityDAO;
        this.districtDAO = districtDAO;
        this.communeDAO = communeDAO;
        this.userAddressDAO = userAddressDAO;
        this.userDAO = userDAO;
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
        String cityId = request.getCityId();
        if (cityId == null || cityId.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_CITY);
        }

        List<District> districts = districtDAO.findByCityId(cityId);
        if (districts.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_CITY);
        }

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
        String districtId = request.getDistrictId();
        if (districtId == null || districtId.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_DISTRICT);
        }

        List<Commune> communes = communeDAO.findByDistrictId(districtId);
        if (communes.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_DISTRICT);
        }

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

    @Override
    public String getAddressFormatted(Long addressId) {
        if (addressId == null) {
            return null;
        }
        Optional<UserAddress> optionalUserAddress = userAddressDAO.findById(addressId);
        if (optionalUserAddress.isEmpty()) {
            return null;
        }
        UserAddress userAddress = optionalUserAddress.get();
        Commune commune = communeDAO.findById(userAddress.getCommuneId()).get();
        District district = districtDAO.findById(commune.getDistrictId()).get();
        City city = cityDAO.findById(district.getCityId()).get();
        return userAddress.getStreetAddress() + COMMA + commune.getName() + COMMA + district.getName() + COMMA + city.getName();
    }

    @Override
    public void saveNewMainAddress(MainAddressDTO mainAddressDTO) {
        Optional<User> optionalUser = userDAO.findByUsername(mainAddressDTO.getUsername());
        Optional<Commune> optionalCommune = communeDAO.findById(mainAddressDTO.getCommuneId());

        if (optionalUser.isPresent() && optionalCommune.isPresent()) {
            User user = optionalUser.get();
            Commune commune = optionalCommune.get();

            deleteOldMainAddress(user);

            UserAddress userAddress = new UserAddress();
            userAddress.setUserId(user.getId());
            userAddress.setMainAddress(true);
            userAddress.setStreetAddress(mainAddressDTO.getStreetAddress());
            userAddress.setName(mainAddressDTO.getFullName());
            userAddress.setPhone(mainAddressDTO.getPhone());
            userAddress.setCommuneId(commune.getId());
            userAddressDAO.save(userAddress);
        }
    }

    private void deleteOldMainAddress(User user) {
        Optional<UserAddress> optionalOldAddress = userAddressDAO
                .findByUserIdAndIsMainAddressAndDeletedAtIsNull(user.getId(), true);
        optionalOldAddress.ifPresent(userAddress -> userAddress.setDeletedAt(LocalDateTime.now()));
    }
}
