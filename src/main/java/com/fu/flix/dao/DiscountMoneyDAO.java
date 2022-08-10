package com.fu.flix.dao;

import com.fu.flix.entity.DiscountMoney;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscountMoneyDAO extends JpaRepository<DiscountMoney, Long> {
    Optional<DiscountMoney> findByVoucherId(Long voucherId);
}
