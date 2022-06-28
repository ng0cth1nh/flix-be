package com.fu.flix.dao;

import com.fu.flix.entity.RepairRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepairRequestDAO extends JpaRepository<RepairRequest, Long> {
    Optional<RepairRequest> findByRequestCode(String requestCode);
    List<RepairRequest> findByUserIdAndStatusIdOrderByCreatedAtDesc(Long userId, String statusId);
}
