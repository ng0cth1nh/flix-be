package com.fu.flix.dao;

import com.fu.flix.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherDAO extends JpaRepository<Voucher, Long> {
}
