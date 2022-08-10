package com.fu.flix.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDTO {
    private Long id;
    private String icon;
    private String categoryName;
    private String status;
    private String image;
    private String description;
}
