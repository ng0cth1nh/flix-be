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
    private Long price;
    private Long actualPrice;
    private String date;
}
