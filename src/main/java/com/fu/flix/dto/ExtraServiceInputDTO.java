package com.fu.flix.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ExtraServiceInputDTO {
    private String name;
    private String description;
    private Long price;
    private Integer insuranceTime;
}
