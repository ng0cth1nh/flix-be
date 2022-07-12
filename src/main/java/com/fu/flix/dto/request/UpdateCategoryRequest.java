package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UpdateCategoryRequest extends DataRequest{
    private Long id;
    private MultipartFile icon;
    private String categoryName;
    private String description;
    private Boolean isActive;
}
