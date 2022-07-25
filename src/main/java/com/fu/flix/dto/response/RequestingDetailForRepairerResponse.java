package com.fu.flix.dto.response;

import lombok.Data;

@Data
public class RequestingDetailForRepairerResponse {
    private String status;
    private String serviceImage;
    private Long serviceId;
    private String serviceName;
    private Long customerId;
    private String avatar;
    private String customerAddress;
    private String customerPhone;
    private String customerName;
    private String expectFixingTime;
    private String requestDescription;
    private String voucherDescription;
    private String voucherDiscount;
    private String paymentMethod;
    private String date;
    private Long totalPrice;
    private Long actualPrice;
    private Long vatPrice;
    private String requestCode;
    private Long inspectionPrice;
    private Long totalDiscount;
    private String approvedTime;
}
