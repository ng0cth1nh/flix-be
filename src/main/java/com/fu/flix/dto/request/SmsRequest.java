package com.fu.flix.dto.request;

import com.fu.flix.constant.enums.OTPType;
import lombok.Data;

@Data
public class SmsRequest {
    private String phoneNumberFormatted;
    private String username;
    private OTPType otpType;
}
