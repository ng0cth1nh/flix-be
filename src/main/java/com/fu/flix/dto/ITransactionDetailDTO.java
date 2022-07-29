package com.fu.flix.dto;

public interface ITransactionDetailDTO {
    Long getId();

    String getRequestCode();

    String getVnpTransactionNo();

    Long getAmount();

    String getTransactionType();

    String getFullName();

    String getPhone();

    String getPayDate();

    String getBankCode();

    String getCardType();

    String getOrderInfo();

    String getVnpBankTranNo();

    String getResponseResult();
}
