package com.fu.flix.dao;

import com.fu.flix.dto.IAccessoryDTO;
import com.fu.flix.entity.Accessory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccessoryDAO extends JpaRepository<Accessory, Long> {
    @Query(value = "SELECT * FROM accessories " +
            "WHERE id IN (:accessoryIds) " +
            "AND service_id = :serviceId ", nativeQuery = true)
    List<Accessory> findAccessories(List<Long> accessoryIds, Long serviceId);

    @Query(value = "SELECT * FROM accessories " +
            "WHERE service_id = :serviceId " +
            "AND name LIKE %:keyword% " +
            "ORDER BY id DESC", nativeQuery = true)
    List<Accessory> searchAccessoriesByService(String keyword, Long serviceId);

    List<Accessory> findByServiceId(Long serviceId);

    @Query(value = "SELECT a.id, a.name, a.description, a.price, a.insurance_time as insuranceTime, a.manufacture, a.country, sv.name as serviceName " +
            "FROM accessories a " +
            "JOIN services sv " +
            "ON a.service_id = sv.id " +
            "WHERE a.name " +
            "LIKE %:keyword% " +
            "ORDER BY a.id DESC", nativeQuery = true)
    List<IAccessoryDTO> searchAccessories(String keyword);

    @Query(value = "SELECT a.id, a.name, a.description, a.price, a.insurance_time as insuranceTime, a.manufacture, a.country, sv.name as serviceName " +
            "FROM accessories a " +
            "JOIN services sv " +
            "ON a.service_id = sv.id " +
            "ORDER BY a.id DESC " +
            "limit :limit offset :offset", nativeQuery = true)
    List<IAccessoryDTO> findAllByOrderByIdDesc(Integer limit, Integer offset);

    @Query(value = "SELECT * FROM accessories WHERE name LIKE :accessoryName AND service_id = :serviceId", nativeQuery = true)
    Optional<Accessory> findByAccessoryNameAndServiceId(String accessoryName, Long serviceId);
}
