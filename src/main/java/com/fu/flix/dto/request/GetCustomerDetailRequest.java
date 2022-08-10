package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetCustomerDetailRequest extends DataRequest {
    private Long customerId;
}
