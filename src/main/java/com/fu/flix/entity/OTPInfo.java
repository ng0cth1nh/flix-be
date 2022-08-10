package com.fu.flix.entity;

import com.fu.flix.constant.enums.OTPType;
import lombok.Data;

@Data
public class OTPInfo {
    private String username;
    private Integer otp;
    private OTPType otpType;
}
