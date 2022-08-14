package com.fu.flix.dao;

import com.fu.flix.dto.IStatisticalRequestDTO;
import com.fu.flix.entity.RepairRequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface RepairRequestHistoryDAO extends JpaRepository<RepairRequestHistory, Long> {
    @Query(value = "SELECT " +
            "(SELECT count(distinct type, request_code) " +
            "FROM repair_request_histories " +
            "WHERE type = 'PENDING_REQUEST' " +
            "AND created_at >= :start " +
            "AND created_at < :end) as totalPendingRequest, " +
            "(SELECT count(distinct type, request_code) " +
            "FROM repair_request_histories " +
            "WHERE type = 'APPROVED_REQUEST' " +
            "AND created_at >= :start " +
            "AND created_at < :end) as totalApprovedRequest, " +
            "(SELECT count(distinct type, request_code) " +
            "FROM repair_request_histories " +
            "WHERE type = 'FIXING_REQUEST' " +
            "AND created_at >= :start " +
            "AND created_at < :end) as totalFixingRequest, " +
            "(SELECT count(distinct type, request_code) " +
            "FROM repair_request_histories " +
            "WHERE type = 'DONE_REQUEST' " +
            "AND created_at >= :start " +
            "AND created_at < :end) as totalDoneRequest, " +
            "(SELECT count(distinct type, request_code) " +
            "FROM repair_request_histories " +
            "WHERE type = 'PAYMENT_WAITING_REQUEST' " +
            "AND created_at >= :start " +
            "AND created_at < :end) as totalPaymentWaitingRequest, " +
            "(SELECT count(distinct type, request_code) " +
            "FROM repair_request_histories " +
            "WHERE type = 'CANCELLED_REQUEST' " +
            "AND created_at >= :start " +
            "AND created_at < :end) as totalCancelRequest", nativeQuery = true)
    IStatisticalRequestDTO findStatisticalRequest(LocalDateTime start, LocalDateTime end);
}
