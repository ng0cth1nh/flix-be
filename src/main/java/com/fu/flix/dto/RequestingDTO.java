package com.fu.flix.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestingDTO {
    private String customerName;
    private String avatar;
    private String serviceName;
    private String expectFixingTime;
    private String address;
    private String description;
    private String requestCode;
    private String iconImage;
    private String createdAt;
}
