package com.fu.flix.dao;

import com.fu.flix.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionHistoryDAO extends JpaRepository<TransactionHistory, Long> {
    Optional<TransactionHistory> findByRequestCodeAndType(String requestCode, String type);
}
