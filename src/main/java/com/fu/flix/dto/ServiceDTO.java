package com.fu.flix.dto;

import lombok.Data;

@Data
public class ServiceDTO {
    private Long id;
    private String serviceName;
    private String icon;
    private String image;
    private Long price;
    private String status;
}
