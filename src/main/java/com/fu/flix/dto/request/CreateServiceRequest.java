package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CreateServiceRequest extends DataRequest {
    private MultipartFile icon;
    private String serviceName;
    private Long categoryId;
    private Long inspectionPrice;
    private String description;
    private Boolean isActive;
    private MultipartFile image;
}
