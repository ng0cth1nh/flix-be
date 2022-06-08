package com.fu.flix.dao;

import com.fu.flix.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceDAO extends JpaRepository<Service, Long> {
    List<Service> findByMajorId(Long majorId);
}
