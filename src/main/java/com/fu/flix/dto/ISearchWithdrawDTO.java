package com.fu.flix.dto;

public interface ISearchWithdrawDTO {
    Long getRepairerId();

    Long getTransactionId();

    String getRepairerName();

    String getRepairerPhone();

    String getWithdrawType();

    String getTransactionCode();

    Long getAmount();
}
