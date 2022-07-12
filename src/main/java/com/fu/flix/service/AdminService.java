package com.fu.flix.service;

import com.fu.flix.dto.request.GetAdminProfileRequest;
import com.fu.flix.dto.response.GetAdminProfileResponse;
import org.springframework.http.ResponseEntity;

public interface AdminService {
    ResponseEntity<GetAdminProfileResponse> getAdminProfile(GetAdminProfileRequest request);
}
