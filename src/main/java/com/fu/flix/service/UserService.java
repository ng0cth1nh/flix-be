package com.fu.flix.service;

import com.fu.flix.dto.request.DeleteAddressRequest;
import com.fu.flix.dto.request.EditAddressRequest;
import com.fu.flix.dto.request.MainAddressRequest;
import com.fu.flix.dto.request.UserAddressRequest;
import com.fu.flix.dto.response.DeleteAddressResponse;
import com.fu.flix.dto.response.EditAddressResponse;
import com.fu.flix.dto.response.MainAddressResponse;
import com.fu.flix.dto.response.UserAddressResponse;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<MainAddressResponse> getMainAddress(MainAddressRequest request);

    ResponseEntity<UserAddressResponse> getUserAddresses(UserAddressRequest request);

    ResponseEntity<DeleteAddressResponse> deleteUserAddress(DeleteAddressRequest request);

    ResponseEntity<EditAddressResponse> editUserAddress(EditAddressRequest request);
}
