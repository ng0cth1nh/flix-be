package com.fu.flix.dao;

import com.fu.flix.entity.Service;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ServiceDAO extends JpaRepository<Service, Long> {
    List<Service> findByMajorId(Long majorId);

    @Query(value = "SELECT * FROM services " +
            "WHERE name LIKE %:keyword%", nativeQuery = true)
    List<Service> searchServices(String keyword);
}
