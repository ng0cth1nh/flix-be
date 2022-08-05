package com.fu.flix.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryRequestForRepairerDTO {
    private String requestCode;
    private String status;
    private String image;
    private String serviceName;
    private Long serviceId;
    private String description;
    private Double price;
    private Double actualPrice;
    private String date;
}
