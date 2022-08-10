package com.fu.flix.dto.request;

import com.fu.flix.dto.PhoneDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendForgotPassOTPRequest extends PhoneDTO {
    private String roleType;
}
