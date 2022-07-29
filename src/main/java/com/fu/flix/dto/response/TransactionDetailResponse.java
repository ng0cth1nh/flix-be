package com.fu.flix.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionDetailResponse {
    private Long id;
    private String requestCode;
    private String vnpTransactionNo;
    private Long amount;
    private String transactionType;
    private String fullName;
    private String phone;
    private String payDate;
    private String bankCode;
    private String cardType;
    private String orderInfo;
    private String vnpBankTranNo;
    private String responseResult;
}
