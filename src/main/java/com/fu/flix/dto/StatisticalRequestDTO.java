package com.fu.flix.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticalRequestDTO {
    private String date;
    private long totalPendingRequest;
    private long totalApprovedRequest;
    private long totalFixingRequest;
    private long totalDoneRequest;
    private long totalPaymentWaitingRequest;
    private long totalCancelRequest;
}
