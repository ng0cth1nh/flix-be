package com.fu.flix.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
public class CFRegisterCustomerRequest extends OTPRequest implements Serializable {
    MultipartFile avatar;
}
