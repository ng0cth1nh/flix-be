package com.fu.flix.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetInvoiceResponse {
    private String customerName;
    private String customerAvatar;
    private String customerPhone;
    private String customerAddress;
    private String repairerName;
    private String repairerAvatar;
    private String repairerPhone;
    private String repairerAddress;
    private Long totalExtraServicePrice;
    private Long totalAccessoryPrice;
    private Long totalSubServicePrice;
    private Long inspectionPrice;
    private Long totalDiscount;
    private String expectFixingTime;
    private String voucherDescription;
    private String voucherDiscount;
    private String paymentMethod;
    private String requestCode;
    private String createdAt;
    private Long actualPrice;
    private Long totalPrice;
    private Long vatPrice;
    private String approvedTime;
    private String serviceName;
    private String serviceImage;
    private Long serviceId;
    private String status;
}
