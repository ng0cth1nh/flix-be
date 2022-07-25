package com.fu.flix.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetCustomerDetailResponse {
    private String avatar;
    private String customerName;
    private String customerPhone;
    private String status;
    private String dateOfBirth;
    private Boolean gender;
    private String email;
    private String address;
    private String createdAt;
}
