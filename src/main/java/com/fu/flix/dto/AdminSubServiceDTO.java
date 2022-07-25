package com.fu.flix.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminSubServiceDTO {
    private Long id;
    private String subServiceName;
    private Long price;
    private String status;
}
