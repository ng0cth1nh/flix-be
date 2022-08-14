package com.fu.flix.dao;

import com.fu.flix.entity.RepairRequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface RepairRequestHistoryDAO extends JpaRepository<RepairRequestHistory, Long> {

    @Query(value = "SELECT count(distinct type, request_code) " +
            "FROM repair_request_histories " +
            "WHERE type = :type " +
            "AND created_at >= :start " +
            "AND created_at < :end", nativeQuery = true)
    long countTotalRequestHistoriesByType(LocalDateTime start, LocalDateTime end, String type);
}
