package com.fu.flix.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticalRepairerAccountDTO {
    private String date;
    private long totalNewAccount;
    private long totalBanAccount;
    private long totalRejectedAccount;
    private long totalApprovedAccount;
}
