package com.fu.flix.dao;

import com.fu.flix.dto.IRepairerComment;
import com.fu.flix.dto.IRepairerProfile;
import com.fu.flix.dto.ISuccessfulRepair;
import com.fu.flix.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentDAO extends JpaRepository<Comment, Long> {

    @Query(value = "SELECT * FROM comments " +
            "WHERE request_code = :requestCode " +
            "AND type = :type", nativeQuery = true)
    Optional<Comment> findComment(String requestCode, String type);

    @Query(value = "SELECT u.full_name as repairerName, avg(c.rating) as rating, r.experience, DATE_FORMAT(r.accepted_account_at,'%d/%m/%Y') as joinAt " +
            "FROM comments c " +
            "JOIN repair_requests_matching rrm " +
            "ON c.request_code = rrm.request_code " +
            "JOIN users u " +
            "ON rrm.repairer_id = u.id " +
            "JOIN repairers r " +
            "ON rrm.repairer_id = r.user_id " +
            "WHERE rrm.repairer_id = :repairerId " +
            "AND c.type = 'CUSTOMER_COMMENT'", nativeQuery = true)
    IRepairerProfile findRepairerProfile(Long repairerId);

    @Query(value = "SELECT count(*) as successfulRepair " +
            "FROM invoices i " +
            "JOIN repair_requests_matching rrm " +
            "ON i.request_code = rrm.request_code " +
            "WHERE rrm.repairer_id = :repairerId " +
            "AND i.status_id = 'DO'", nativeQuery = true)
    ISuccessfulRepair findSuccessfulRepair(Long repairerId);

    @Query(value = "SELECT rr.user_id as customerId, u.full_name as customerName, c.rating, c.comment " +
            "FROM comments c " +
            "JOIN repair_requests_matching rrm " +
            "ON c.request_code = rrm.request_code " +
            "JOIN repair_requests rr " +
            "ON c.request_code = rr.request_code " +
            "JOIN users u " +
            "ON u.id = rr.user_id " +
            "WHERE rrm.repairer_id = :repairerId " +
            "AND c.type = 'CUSTOMER_COMMENT' " +
            "limit :limit offset :offset", nativeQuery = true)
    List<IRepairerComment> findRepairComments(Long repairerId, Integer limit, Integer offset);
}
