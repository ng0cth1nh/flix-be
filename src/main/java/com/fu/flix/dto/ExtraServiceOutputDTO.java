package com.fu.flix.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtraServiceOutputDTO {
    private Long id;
    private String name;
    private String description;
    private Long price;
    private Integer insuranceTime;
}
