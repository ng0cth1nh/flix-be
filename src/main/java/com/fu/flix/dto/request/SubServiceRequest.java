package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubServiceRequest extends DataRequest {
    private Long serviceId;
}
