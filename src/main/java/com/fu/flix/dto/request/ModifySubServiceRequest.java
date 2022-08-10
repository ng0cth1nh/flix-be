package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModifySubServiceRequest extends DataRequest{
    private String subServiceName;
    private Long price;
    private Long serviceId;
    private String description;
    private Boolean isActive;
}
