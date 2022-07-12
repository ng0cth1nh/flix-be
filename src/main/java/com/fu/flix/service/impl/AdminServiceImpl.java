package com.fu.flix.service.impl;

import com.fu.flix.dto.request.GetAdminProfileRequest;
import com.fu.flix.dto.response.GetAdminProfileResponse;
import com.fu.flix.entity.User;
import com.fu.flix.service.AdminService;
import com.fu.flix.service.ValidatorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {
    private final ValidatorService validatorService;

    public AdminServiceImpl(ValidatorService validatorService) {
        this.validatorService = validatorService;
    }

    @Override
    public ResponseEntity<GetAdminProfileResponse> getAdminProfile(GetAdminProfileRequest request) {
        User admin = validatorService.getUserValidated(request.getUserId());
        GetAdminProfileResponse response = new GetAdminProfileResponse();
        response.setFullName(admin.getFullName());
        response.setPhone(admin.getPhone());
        response.setEmail(admin.getEmail());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
