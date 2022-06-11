package com.fu.flix.dto;

import lombok.Data;

@Data
public class HistoryRepairRequestDTO {
    private String requestCode;
    private String serviceName;
    private String description;
    private Double price;
    private String date;
}
