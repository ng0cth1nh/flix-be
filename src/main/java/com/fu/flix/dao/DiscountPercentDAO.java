package com.fu.flix.dao;

import com.fu.flix.entity.DiscountPercent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscountPercentDAO extends JpaRepository<DiscountPercent, Long> {
    Optional<DiscountPercent> findByVoucherId(Long voucherId);
}
