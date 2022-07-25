package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCustomerProfileRequest extends DataRequest{
    private String fullName;
    private String dateOfBirth;
    private Boolean gender;
    private String email;
}
