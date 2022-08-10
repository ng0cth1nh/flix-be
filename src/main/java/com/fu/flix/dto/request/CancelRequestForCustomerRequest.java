package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelRequestForCustomerRequest extends DataRequest {
    private String requestCode;
    private String reason;
}
