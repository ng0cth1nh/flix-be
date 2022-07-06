package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OTPRequest {
    private Integer otp;
    private String phone;
}
