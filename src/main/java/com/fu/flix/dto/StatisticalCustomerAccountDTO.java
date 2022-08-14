package com.fu.flix.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticalCustomerAccountDTO {
    private String date;
    private long totalNewAccount;
    private long totalBanAccount;
}
