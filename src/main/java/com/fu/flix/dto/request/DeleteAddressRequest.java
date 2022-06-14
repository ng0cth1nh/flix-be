package com.fu.flix.dto.request;

import lombok.Data;

@Data
public class DeleteAddressRequest extends DataRequest {
    private Long addressId;
}
