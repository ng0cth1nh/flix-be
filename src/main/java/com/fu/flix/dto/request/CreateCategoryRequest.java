package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CreateCategoryRequest extends DataRequest {
    private MultipartFile icon;
    private MultipartFile image;
    private String categoryName;
    private String description;
    private Boolean isActive;
}

