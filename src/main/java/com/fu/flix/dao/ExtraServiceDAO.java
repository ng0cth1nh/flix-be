package com.fu.flix.dao;

import com.fu.flix.entity.ExtraService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtraServiceDAO extends JpaRepository<ExtraService, Long> {
}
