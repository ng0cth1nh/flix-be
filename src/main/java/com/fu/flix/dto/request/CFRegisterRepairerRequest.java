package com.fu.flix.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
public class CFRegisterRepairerRequest extends OTPRequest implements Serializable {
    private MultipartFile avatar;
}
