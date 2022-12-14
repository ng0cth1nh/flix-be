package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestingRepairRequest extends DataRequest {
    private Long serviceId;
    private Long addressId;
    private String expectFixingDay;
    private String description;
    private Long voucherId;
    private String paymentMethodId;
}
