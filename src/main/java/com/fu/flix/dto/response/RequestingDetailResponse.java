package com.fu.flix.dto.response;

import lombok.Data;

@Data
public class RequestingDetailResponse {
    private String customerName;
    private String avatar;
    private Long customerId;
    private Long serviceId;
    private Long addressId;
    private String expectFixingTime;
    private String description;
    private Long voucherId;
    private String paymentMethodId;
}
