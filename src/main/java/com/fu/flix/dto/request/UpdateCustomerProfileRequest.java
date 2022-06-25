package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCustomerProfileRequest extends DataRequest{
    private String fullName;
    private String dateOfBirth;
    private boolean gender;
    private String email;
}
