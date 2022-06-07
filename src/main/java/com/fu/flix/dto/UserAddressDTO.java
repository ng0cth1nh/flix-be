package com.fu.flix.dto;

import lombok.Data;

@Data
public class UserAddressDTO {
    private String addressCode;
    private String addressName;
    private String customerName;
    private String phone;
    private boolean isMainAddress;
}
