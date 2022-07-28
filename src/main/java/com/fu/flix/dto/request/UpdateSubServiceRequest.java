package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSubServiceRequest extends ModifySubServiceRequest {
    private Long subServiceId;
}
