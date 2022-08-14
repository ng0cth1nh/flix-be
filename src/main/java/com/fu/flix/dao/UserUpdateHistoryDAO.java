package com.fu.flix.dao;

import com.fu.flix.entity.UserUpdateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserUpdateHistoryDAO extends JpaRepository<UserUpdateHistory, Long> {
    @Query(value = "SELECT count(distinct uuh.user_id) " +
            "FROM user_update_histories uuh " +
            "JOIN user_roles ur " +
            "ON ur.user_id = uuh.user_id " +
            "WHERE type = 'BAN_ACCOUNT' " +
            "AND ur.role_id IN (:roleIds) " +
            "AND uuh.created_at >= :start " +
            "AND uuh.created_at < :end", nativeQuery = true)
    long countTotalBannedAccountHistories(LocalDateTime start, LocalDateTime end, String... roleIds);

    @Query(value = "SELECT count(distinct user_id) " +
            "FROM user_update_histories " +
            "WHERE type = 'REJECT_CV' " +
            "AND created_at >= :start " +
            "AND created_at < :end", nativeQuery = true)
    long countTotalRejectedAccountHistories(LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT count(distinct user_id) " +
            "FROM user_update_histories " +
            "WHERE type = 'ACCEPT_CV' " +
            "AND created_at >= :start " +
            "AND created_at < :end", nativeQuery = true)
    long countTotalApprovedAccountHistories(LocalDateTime start, LocalDateTime end);
}
