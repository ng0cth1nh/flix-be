package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithdrawDetailRequest extends DataRequest {
    private Long transactionId;
}
