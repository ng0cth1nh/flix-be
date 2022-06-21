package com.fu.flix.dao;

import com.fu.flix.entity.Repairer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepairerDAO extends JpaRepository<Repairer, Long> {
}
