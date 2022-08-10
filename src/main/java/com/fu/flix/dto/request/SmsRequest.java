package com.fu.flix.dto.request;

import com.fu.flix.constant.enums.OTPType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsRequest {
    private String phoneNumberFormatted;
    private String username;
    private OTPType otpType;
}
