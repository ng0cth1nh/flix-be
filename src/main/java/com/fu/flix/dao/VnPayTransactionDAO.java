package com.fu.flix.dao;

import com.fu.flix.entity.VnPayTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VnPayTransactionDAO extends JpaRepository<VnPayTransaction, Long> {
    Optional<VnPayTransaction> findByVnpTxnRefAndResponseCode(String vnpTxnRef, String responseCode);
}
