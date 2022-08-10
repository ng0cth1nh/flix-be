package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepairerDepositUrlRequest extends DataRequest {
    private String orderInfo;
    private String bankCode;
    private Long amount;
}
