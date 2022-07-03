package com.fu.flix.dto;

import lombok.Data;

@Data
public class ServiceDTO {
    private Long serviceId;
    private String serviceName;
    private String imageUrl;
    private Long price;
}
