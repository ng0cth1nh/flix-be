package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChooseMainAddressRequest extends DataRequest {
    private Long addressId;
}
