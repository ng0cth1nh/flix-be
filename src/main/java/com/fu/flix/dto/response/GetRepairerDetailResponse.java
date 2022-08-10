package com.fu.flix.dto.response;

import com.fu.flix.dto.IAdminCheckRegisterServiceDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetRepairerDetailResponse {
    private Long id;
    private String avatar;
    private String repairerName;
    private String repairerPhone;
    private String status;
    private String dateOfBirth;
    private Boolean gender;
    private String email;
    private String address;
    private String createdAt;
    private Integer experienceYear;
    private String experienceDescription;
    private String identityCardNumber;
    private String identityCardType;
    private String frontImage;
    private String backSideImage;
    private String acceptedAccountAt;
    private List<String> certificates;
    private String cvStatus;
    private List<IAdminCheckRegisterServiceDTO> registerServices;
}
