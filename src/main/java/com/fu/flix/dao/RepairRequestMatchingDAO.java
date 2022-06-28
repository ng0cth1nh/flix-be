package com.fu.flix.dao;

import com.fu.flix.dto.IHistoryRequestForRepairerDTO;
import com.fu.flix.entity.RepairRequestMatching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepairRequestMatchingDAO extends JpaRepository<RepairRequestMatching, String> {
    Optional<RepairRequestMatching> findByRequestCode(String requestCode);

    @Query(value = "SELECT rrm.request_code as requestCode, s.name as status, i.url as image, sv.name as serviceName, rr.description, iv.total_price as price, iv.actual_proceeds as actualPrice, rr.created_at as createdAt " +
            "FROM repair_requests_matching rrm " +
            "JOIN repair_requests rr " +
            "ON rrm.request_code = rr.request_code " +
            "JOIN status s " +
            "ON rr.status_id = s.id " +
            "JOIN services sv " +
            "ON rr.service_id = sv.id " +
            "JOIN images i " +
            "ON sv.image_id = i.id " +
            "JOIN invoices iv " +
            "ON rr.request_code = iv.request_code " +
            "WHERE rrm.repairer_id = :repairerId " +
            "AND rr.status_id = :statusId", nativeQuery = true)
    List<IHistoryRequestForRepairerDTO> findRequestHistoriesForRepairerByStatus(Long repairerId, String statusId);
}
