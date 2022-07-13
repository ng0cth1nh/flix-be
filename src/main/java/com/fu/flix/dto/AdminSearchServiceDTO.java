package com.fu.flix.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminSearchServiceDTO {
    private Long serviceId;
    private String serviceName;
    private String icon;
    private String image;
    private String status;
}
