package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class CFRegisterRepairerRequest extends OTPRequest {
    private MultipartFile avatar;
    private String fullName;
    private String password;
    private String communeId;
    private String streetAddress;
    private String identityCardNumber;
    private String identityCardType;
    private MultipartFile frontImage;
    private MultipartFile backSideImage;
    private Integer experienceYear;
    private String experienceDescription;
    private List<MultipartFile> certificates;
    private Boolean gender;
    private String dateOfBirth;
}
