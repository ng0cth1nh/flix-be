package com.fu.flix.dto;

public interface IWithdrawHistoryDTO {
    Long getRepairerId();

    Long getTransactionId();

    String getRepairerName();

    String getRepairerPhone();

    String getWithdrawType();

    String getTransactionCode();

    String getAmount();
}
