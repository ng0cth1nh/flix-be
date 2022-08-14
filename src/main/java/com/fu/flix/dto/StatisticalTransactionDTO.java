package com.fu.flix.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticalTransactionDTO {
    private String date;
    private long totalProfit;
    private long totalRevenue;
}
