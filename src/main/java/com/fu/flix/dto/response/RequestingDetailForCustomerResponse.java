package com.fu.flix.dto.response;

import lombok.Data;

@Data
public class RequestingDetailForCustomerResponse {
    private Long serviceId;
    private String serviceName;
    private Long addressId;
    private String expectFixingDay;
    private String description;
    private Long voucherId;
    private String paymentMethodId;
    private String date;
    private Double price;
}
