package com.fu.flix.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetailCategoryResponse {
    private Long categoryId;
    private String icon;
    private String image;
    private String categoryName;
    private String description;
    private boolean isActive;
}
