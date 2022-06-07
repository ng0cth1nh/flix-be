package com.fu.flix.dto.request;

import lombok.Data;

@Data
public class OTPRequest {
    private int otp;
    private String username;
}
