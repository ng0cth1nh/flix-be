package com.fu.flix.dao;

import com.fu.flix.entity.WithdrawRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WithdrawRequestDAO extends JpaRepository<WithdrawRequest, Long> {
}
