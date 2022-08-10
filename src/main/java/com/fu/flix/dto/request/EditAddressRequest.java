package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditAddressRequest extends DataRequest{
    private Long addressId;
    private String name;
    private String phone;
    private String communeId;
    private String streetAddress;
}
