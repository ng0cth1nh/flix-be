package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetFixedServiceRequest extends DataRequest {
    private String requestCode;
}
