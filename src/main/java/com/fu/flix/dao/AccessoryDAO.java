package com.fu.flix.dao;

import com.fu.flix.entity.Accessory;
import com.fu.flix.entity.SubService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessoryDAO extends JpaRepository<Accessory, Long> {
    @Query(value = "SELECT * FROM accessories where id in (:accessoryIds) AND service_id = :serviceId", nativeQuery = true)
    List<SubService> findAccessories(List<Long> accessoryIds, Long serviceId);
}
