package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerPaymentUrlRequest extends DataRequest {
    private String orderInfo;
    private String bankCode;
    private String requestCode;
}
