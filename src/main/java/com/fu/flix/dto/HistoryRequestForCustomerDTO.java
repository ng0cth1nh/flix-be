package com.fu.flix.dto;

import lombok.Data;

@Data
public class HistoryRequestForCustomerDTO {
    private String requestCode;
    private String status;
    private String image;
    private Long serviceId;
    private String serviceName;
    private String description;
    private Double price;
    private Double actualPrice;
    private String date;
}
