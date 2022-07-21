package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CFRegisterUserRequest extends OTPRequest {
    private String fullName;
    private String communeId;
    private String streetAddress;
}
