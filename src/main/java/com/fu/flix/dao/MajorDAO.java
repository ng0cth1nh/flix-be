package com.fu.flix.dao;

import com.fu.flix.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MajorDAO extends JpaRepository<Major, Long> {
}
