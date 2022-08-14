package com.fu.flix.dto;

public interface IStatisticalRequestDTO {
    long getTotalPendingRequest();

    long getTotalApprovedRequest();

    long getTotalFixingRequest();

    long getTotalDoneRequest();

    long getTotalPaymentWaitingRequest();

    long getTotalCancelRequest();
}
