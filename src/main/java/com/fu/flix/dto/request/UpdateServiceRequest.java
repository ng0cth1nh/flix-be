package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateServiceRequest extends ModifyServiceRequest {
    private Long serviceId;
}
