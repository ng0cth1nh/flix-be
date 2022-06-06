package com.fu.flix.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String phone;
    private String password;
    private String cityId;
    private String districtId;
    private String communeId;
    private String streetAddress;
}