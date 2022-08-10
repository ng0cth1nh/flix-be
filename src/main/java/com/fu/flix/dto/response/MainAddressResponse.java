package com.fu.flix.dto.response;

import lombok.Data;

@Data
public class MainAddressResponse {
    private Long addressId;
    private String addressName;
    private String customerName;
    private String phone;
}
