package com.fu.flix.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithdrawDetailResponse {
    private Long transactionId;
    private String repairerName;
    private String repairerPhone;
    private String withdrawType;
    private String transactionCode;
    private Long amount;
    private String bankCode;
    private String bankAccountNumber;
    private String bankAccountName;
}
