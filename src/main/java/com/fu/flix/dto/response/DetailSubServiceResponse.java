package com.fu.flix.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetailSubServiceResponse {
    private Long subServiceId;
    private String subServiceName;
    private Long price;
    private Long serviceId;
    private String description;
    private boolean isActive;
}
