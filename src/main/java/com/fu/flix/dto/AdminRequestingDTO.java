package com.fu.flix.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminRequestingDTO {
    private String requestCode;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private Long repairerId;
    private String repairerName;
    private String repairerPhone;
    private String status;
    private String createdAt;
}
