package com.fu.flix.service.impl;

import com.fu.flix.dao.*;
import com.fu.flix.dto.UserAddressDTO;
import com.fu.flix.dto.request.DeleteAddressRequest;
import com.fu.flix.dto.request.EditAddressRequest;
import com.fu.flix.dto.request.MainAddressRequest;
import com.fu.flix.dto.request.UserAddressRequest;
import com.fu.flix.dto.response.DeleteAddressResponse;
import com.fu.flix.dto.response.EditAddressResponse;
import com.fu.flix.dto.response.MainAddressResponse;
import com.fu.flix.dto.response.UserAddressResponse;
import com.fu.flix.entity.*;
import com.fu.flix.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fu.flix.constant.Constant.DELETE_ADDRESS_SUCCESS;
import static com.fu.flix.constant.Constant.EDIT_ADDRESS_SUCCESS;

@Service
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
    private final UserAddressDAO userAddressDAO;
    private final UserDAO userDAO;
    private final CommuneDAO communeDAO;
    private final DistrictDAO districtDAO;
    private final CityDAO cityDAO;
    private final String COMMA = ", ";


    public UserServiceImpl(UserAddressDAO userAddressDAO,
                           UserDAO userDAO,
                           CommuneDAO communeDAO,
                           DistrictDAO districtDAO,
                           CityDAO cityDAO) {
        this.userAddressDAO = userAddressDAO;
        this.userDAO = userDAO;
        this.communeDAO = communeDAO;
        this.districtDAO = districtDAO;
        this.cityDAO = cityDAO;
    }

    @Override
    public ResponseEntity<MainAddressResponse> getMainAddress(MainAddressRequest request) {
        User user = userDAO.findByUsername(request.getUsername()).get();
        UserAddress userAddress = userAddressDAO.findByUserIdAndIsMainAddressAndDeletedAtIsNull(user.getId(), true).get();

        MainAddressResponse response = new MainAddressResponse();
        response.setAddressId(userAddress.getId());
        response.setCustomerName(userAddress.getName());
        response.setPhone(userAddress.getPhone());
        response.setAddressName(getAddressFormatted(userAddress.getCommuneId(), userAddress.getStreetAddress()));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UserAddressResponse> getUserAddresses(UserAddressRequest request) {
        User user = userDAO.findByUsername(request.getUsername()).get();
        List<UserAddress> userAddresses = userAddressDAO.findByUserIdAndDeletedAtIsNull(user.getId());

        List<UserAddressDTO> addresses = userAddresses.stream()
                .map(userAddress -> {
                    UserAddressDTO dto = new UserAddressDTO();
                    dto.setAddressId(userAddress.getId());
                    dto.setCustomerName(userAddress.getName());
                    dto.setPhone(userAddress.getPhone());
                    dto.setAddressName(getAddressFormatted(userAddress.getCommuneId(), userAddress.getStreetAddress()));
                    dto.setMainAddress(userAddress.isMainAddress());
                    return dto;
                }).collect(Collectors.toList());

        UserAddressResponse response = new UserAddressResponse();
        response.setAddresses(addresses);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public String getAddressFormatted(String communeId, String streetAddress) {
        Commune commune = communeDAO.findById(communeId).get();
        District district = districtDAO.findById(commune.getDistrictId()).get();
        City city = cityDAO.findById(district.getCityId()).get();
        return streetAddress + COMMA + commune.getName() + COMMA + district.getName() + COMMA + city.getName();
    }

    @Override
    public ResponseEntity<DeleteAddressResponse> deleteUserAddress(DeleteAddressRequest request) {
        User user = userDAO.findByUsername(request.getUsername()).get();
        Optional<UserAddress> optionalUserAddress = userAddressDAO
                .findUserAddressToDelete(user.getId(), request.getAddressId());

        optionalUserAddress.ifPresent(userAddress -> userAddress.setDeletedAt(LocalDateTime.now()));

        DeleteAddressResponse response = new DeleteAddressResponse();
        response.setMessage(DELETE_ADDRESS_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EditAddressResponse> editUserAddress(EditAddressRequest request) {
        Long addressId = request.getAddressId();
        User user = userDAO.findByUsername(request.getUsername()).get();
        Optional<UserAddress> optionalUserAddress = userAddressDAO
                .findUserAddressToEdit(user.getId(), addressId);
        Optional<Commune> optionalCommune = communeDAO.findById(request.getCommuneId());

        if (optionalUserAddress.isPresent() && optionalUserAddress.isPresent()) {
            UserAddress userAddress = optionalUserAddress.get();
            Commune commune = optionalCommune.get();

            userAddress.setName(request.getName());
            userAddress.setPhone(request.getPhone());
            userAddress.setCommuneId(commune.getId());
            userAddress.setStreetAddress(request.getStreetAddress());
        }

        EditAddressResponse response = new EditAddressResponse();
        response.setAddressId(addressId);
        response.setMessage(EDIT_ADDRESS_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
