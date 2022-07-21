package com.fu.flix.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepairerProfileResponse {
    private String fullName;
    private String avatar;
    private String phone;
    private String dateOfBirth;
    private Boolean gender;
    private String email;
    private String role;
    private String experienceDescription;
    private String identityCardNumber;
    private String address;
    private String balance;
}
