package com.fpt.flix.flix_app.models.requests;

import lombok.Data;

@Data
public class OTPRequest extends DataRequest{
    private int otp;
}
