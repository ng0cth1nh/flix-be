package com.fu.flix.dao;

import com.fu.flix.entity.SubService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubServiceDAO extends JpaRepository<SubService, Long> {
    @Query(value = "SELECT * FROM sub_services " +
            "WHERE id IN (:subServiceIds) " +
            "AND service_id = :serviceId " +
            "AND is_active", nativeQuery = true)
    List<SubService> findSubServices(List<Long> subServiceIds, Long serviceId);

    @Query(value = "SELECT * FROM sub_services " +
            "WHERE service_id = :serviceId " +
            "AND name LIKE %:keyword% " +
            "AND is_active " +
            "ORDER BY id DESC", nativeQuery = true)
    List<SubService> searchSubServicesByService(String keyword, Long serviceId);

    List<SubService> findByServiceIdAndIsActive(Long serviceId, Boolean isActive);

    @Query(value = "SELECT * " +
            "FROM sub_services " +
            "WHERE name LIKE %:keyword% " +
            "AND service_id = :serviceId " +
            "ORDER BY id DESC", nativeQuery = true)
    List<SubService> searchSubServicesForAdmin(String keyword, Long serviceId);

    Page<SubService> findByServiceIdOrderByIdDesc(Long serviceId, Pageable pageable);

    long countByServiceId(Long serviceId);

    @Query(value = "SELECT * FROM sub_services WHERE name LIKE :subServiceName AND service_id = :serviceId", nativeQuery = true)
    Optional<SubService> findBySubServiceNameAndServiceId(String subServiceName, Long serviceId);
}
