package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteAddressRequest extends DataRequest {
    private Long addressId;
}
