package com.fu.flix.dto.response;

import lombok.Data;

@Data
public class RequestingDetailForCustomerResponse {
    private String status;
    private String serviceImage;
    private Long serviceId;
    private String serviceName;
    private String customerAddress;
    private String customerPhone;
    private String customerName;
    private String expectFixingDay;
    private String requestDescription;
    private String voucherDescription;
    private String voucherDiscount;
    private String paymentMethod;
    private String date;
    private Long totalPrice;
    private Long actualPrice;
    private Long vatPrice;
    private String requestCode;
    private String repairerAddress;
    private String repairerPhone;
    private String repairerName;
    private Long repairerId;
    private String repairerAvatar;
    private Long inspectionPrice;
    private Long totalDiscount;
}
