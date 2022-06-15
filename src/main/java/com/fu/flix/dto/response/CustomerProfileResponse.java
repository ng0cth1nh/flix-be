package com.fu.flix.dto.response;

import lombok.Data;

@Data
public class CustomerProfileResponse {
    private String fullName;
    private String avatarUrl;
    private String phone;
    private String dateOfBirth;
    private Boolean gender;
    private String email;
}
