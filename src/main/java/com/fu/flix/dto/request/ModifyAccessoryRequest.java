package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModifyAccessoryRequest extends DataRequest{
    private String accessoryName;
    private Long price;
    private Integer insurance;
    private String manufacturer;
    private String country;
    private String description;
    private Long serviceId;
}
