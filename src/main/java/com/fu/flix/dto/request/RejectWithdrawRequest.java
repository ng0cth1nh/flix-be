package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectWithdrawRequest extends DataRequest {
    private Long transactionId;
    private String reason;
}
