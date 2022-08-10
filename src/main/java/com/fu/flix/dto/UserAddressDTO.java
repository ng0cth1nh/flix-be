package com.fu.flix.dto;

import lombok.Data;

@Data
public class UserAddressDTO {
    private Long addressId;
    private String addressName;
    private String customerName;
    private String phone;
    private boolean isMainAddress;
    private String districtId;
    private String cityId;
    private String communeId;
}
