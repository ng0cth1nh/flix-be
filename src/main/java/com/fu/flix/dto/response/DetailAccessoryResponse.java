package com.fu.flix.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetailAccessoryResponse {
    private Long accessoryId;
    private String accessoryName;
    private Long price;
    private Integer insurance;
    private String manufacturer;
    private String country;
    private String description;
    private Long serviceId;
}
