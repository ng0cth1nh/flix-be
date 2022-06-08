package com.fu.flix.dto.request;

import com.fu.flix.constant.enums.RoleType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


@Data
public class CFRegisterRequest extends OTPRequest {
    private MultipartFile avatar;
    private String fullName;
    private String password;
    private String cityId;
    private String districtId;
    private String communeId;
    private String streetAddress;
    private RoleType roleType;
}
