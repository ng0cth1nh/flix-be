package com.fu.flix.dao;

import com.fu.flix.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ServiceDAO extends JpaRepository<Service, Long> {
    List<Service> findByCategoryId(Long categoryId);

    @Query(value = "SELECT * FROM services " +
            "WHERE name LIKE %:keyword%", nativeQuery = true)
    List<Service> searchServices(String keyword);
}
