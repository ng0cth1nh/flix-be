package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepairerWithdrawRequest extends DataRequest {
    private Long amount;
    private String withdrawType;
    private String bankCode;
    private String bankAccountNumber;
    private String bankAccountName;
}
