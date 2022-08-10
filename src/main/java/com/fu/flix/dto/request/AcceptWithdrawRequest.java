package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptWithdrawRequest extends DataRequest {
    private Long transactionId;
}
