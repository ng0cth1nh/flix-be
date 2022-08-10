package com.fu.flix.dao;

import com.fu.flix.entity.Accessory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessoryDAO extends JpaRepository<Accessory, Long> {
    @Query(value = "SELECT * FROM accessories " +
            "WHERE id IN (:accessoryIds) " +
            "AND service_id = :serviceId ", nativeQuery = true)
    List<Accessory> findAccessories(List<Long> accessoryIds, Long serviceId);

    @Query(value = "SELECT * FROM accessories " +
            "WHERE service_id = :serviceId " +
            "AND name LIKE %:keyword% ", nativeQuery = true)
    List<Accessory> searchAccessoriesByService(String keyword, Long serviceId);

    List<Accessory> findByServiceId(Long serviceId);

    @Query(value = "SELECT * FROM accessories " +
            "WHERE name LIKE %:keyword% ", nativeQuery = true)
    List<Accessory> searchAccessories(String keyword);
}
