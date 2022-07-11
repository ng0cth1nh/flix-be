package com.fu.flix.dao;

import com.fu.flix.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BalanceDAO extends JpaRepository<Balance, Long> {
    Optional<Balance> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
