package com.fu.flix.dao;

import com.fu.flix.dto.IRepairerProfile;
import com.fu.flix.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentDAO extends JpaRepository<Comment, Long> {

    @Query(value = "SELECT * FROM comments " +
            "WHERE request_code = :requestCode " +
            "AND type = :type", nativeQuery = true)
    Optional<Comment> findComment(String requestCode, String type);

    @Query(value = "SELECT u.full_name as repairerName, avg(c.rating) as rating, r.experience, DATE_FORMAT(r.accepted_account_at,'%d/%m/%Y') as joinAt, count(i.request_code) as successfulRepair " +
            "FROM comments c " +
            "JOIN repair_requests_matching rrm " +
            "ON c.request_code = rrm.request_code " +
            "JOIN users u " +
            "ON rrm.repairer_id = u.id " +
            "JOIN repairers r " +
            "ON rrm.repairer_id = r.user_id " +
            "JOIN invoices i " +
            "ON c.request_code = i.request_code " +
            "WHERE rrm.repairer_id = :repairerId " +
            "AND c.type = 'CUSTOMER_COMMENT' " +
            "AND i.status_id = 'DO'", nativeQuery = true)
    Optional<IRepairerProfile> findRepairerProfile(Long repairerId);
}
