package com.fu.flix.dao;

import com.fu.flix.entity.Repairer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepairerDAO extends JpaRepository<Repairer, Long> {
    Optional<Repairer> findByUsername(String username);
    Optional<Repairer> findByUserId(Long userId);
}
