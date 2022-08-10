package com.fu.flix.dao;

import com.fu.flix.entity.BankInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankInfoDAO extends JpaRepository<BankInfo, String> {
}
