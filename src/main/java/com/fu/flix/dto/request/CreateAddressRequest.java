package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAddressRequest extends DataRequest {
    private String fullName;
    private String phone;
    private String communeId;
    private String streetAddress;
}
