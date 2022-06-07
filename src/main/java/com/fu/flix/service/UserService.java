package com.fu.flix.service;

import com.fu.flix.dto.request.MainAddressRequest;
import com.fu.flix.dto.response.MainAddressResponse;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<MainAddressResponse> getMainAddress(MainAddressRequest request);
}
