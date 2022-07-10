package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;


@Getter
@Setter
public class CFRegisterCustomerRequest extends OTPRequest {
    private MultipartFile avatar;
    private String fullName;
    private String password;
    private String communeId;
    private String streetAddress;
}
