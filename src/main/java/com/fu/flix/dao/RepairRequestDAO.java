package com.fu.flix.dao;

import com.fu.flix.entity.RepairRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepairRequestDAO extends JpaRepository<RepairRequest, Long> {
}
