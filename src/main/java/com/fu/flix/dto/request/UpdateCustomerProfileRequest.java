package com.fu.flix.dto.request;

import lombok.Data;

@Data
public class UpdateCustomerProfileRequest extends DataRequest{
    private String fullName;
    private String dateOfBirth;
    private boolean gender;
    private String email;
}
