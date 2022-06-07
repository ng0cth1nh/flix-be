package com.fu.flix.dto.response;

import lombok.Data;

@Data
public class MainAddressResponse {
    private String addressCode;
    private String addressName;
    private String customerName;
    private String phone;
}
