package com.fu.flix.dto;

public interface IWithdrawDetail {

    Long getTransactionId();

    String getRepairerName();

    String getRepairerPhone();

    String getWithdrawType();

    String getTransactionCode();

    Long getAmount();

    String getBankCode();

    String getBankAccountNumber();

    String getBankAccountName();
}
