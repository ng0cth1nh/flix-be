package com.fu.flix.dto.response;

import com.fu.flix.dto.IRegisterServiceDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
    private List<IRegisterServiceDTO> registerServices;
}
