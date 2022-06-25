package com.fu.flix.dao;

import com.fu.flix.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BalanceDAO extends JpaRepository<Balance, Long> {
}
