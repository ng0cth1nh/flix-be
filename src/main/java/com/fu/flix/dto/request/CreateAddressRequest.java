package com.fu.flix.dto.request;

import lombok.Data;

@Data
public class CreateAddressRequest extends DataRequest {
    private String fullName;
    private String phone;
    private String communeId;
    private String streetAddress;
}
