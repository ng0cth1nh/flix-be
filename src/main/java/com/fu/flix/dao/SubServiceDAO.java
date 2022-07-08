package com.fu.flix.dao;

import com.fu.flix.entity.SubService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubServiceDAO extends JpaRepository<SubService, Long> {
    @Query(value = "SELECT * FROM sub_services where id in (:subServiceIds) AND service_id = :serviceId", nativeQuery = true)
    List<SubService> findSubServices(List<Long> subServiceIds, Long serviceId);

    @Query(value = "SELECT * FROM sub_services WHERE service_id = :serviceId AND name LIKE %:keyword%", nativeQuery = true)
    List<SubService> searchSubServicesByService(String keyword, Long serviceId);
}
