package com.fu.flix.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetailServiceResponse {
    private Long serviceId;
    private String icon;
    private String serviceName;
    private Long categoryId;
    private Long inspectionPrice;
    private String description;
    private boolean isActive;
    private String image;
}
