package com.fu.flix.dto;

import lombok.Data;

@Data
public class SearchServiceDTO {
    private Long serviceId;
    private String serviceName;
    private String icon;
    private String image;
    private String status;
    private Long price;
}
