package com.fpt.flix.flix_app.models.requests;

import lombok.Data;

@Data
public class OTPRequest {
    private int otp;
    private String username;
}
