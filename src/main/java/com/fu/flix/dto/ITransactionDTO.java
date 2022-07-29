package com.fu.flix.dto;

public interface ITransactionDTO {
    Long getId();

    String getRequestCode();

    String getVnpTransactionNo();

    Long getAmount();

    String getTransactionType();

    String getFullName();

    String getPhone();

    String getPayDate();
}
