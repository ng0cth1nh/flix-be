package com.fu.flix.dto.request;

import lombok.Data;

@Data
public class EditAddressRequest extends DataRequest{
    private Long addressId;
    private String name;
    private String phone;
    private String communeId;
    private String streetAddress;
}
