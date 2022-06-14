package com.fu.flix.service;

import com.fu.flix.dto.request.DeleteAddressRequest;
import com.fu.flix.dto.request.MainAddressRequest;
import com.fu.flix.dto.request.UserAddressRequest;
import com.fu.flix.dto.response.DeleteAddressResponse;
import com.fu.flix.dto.response.MainAddressResponse;
import com.fu.flix.dto.response.UserAddressResponse;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<MainAddressResponse> getMainAddress(MainAddressRequest request);

    ResponseEntity<UserAddressResponse> getUserAddresses(UserAddressRequest request);

    ResponseEntity<DeleteAddressResponse> deleteUserAddress(DeleteAddressRequest request);
}
