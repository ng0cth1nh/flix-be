package com.fu.flix.dao;

import com.fu.flix.entity.RepairRequestMatching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepairRequestMatchingDAO extends JpaRepository<RepairRequestMatching, String> {
    Optional<RepairRequestMatching> findByRequestCode(String requestCode);
}
