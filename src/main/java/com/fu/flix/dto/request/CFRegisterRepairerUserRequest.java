package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class CFRegisterRepairerUserRequest extends CFRegisterUserRequest {
    private MultipartFile avatar;
    private String password;
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
