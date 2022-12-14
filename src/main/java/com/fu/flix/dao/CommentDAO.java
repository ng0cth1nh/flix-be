package com.fu.flix.dao;

import com.fu.flix.dto.IRepairerCommentDTO;
import com.fu.flix.dto.ICustomerGetRepairerDTO;
import com.fu.flix.dto.ISuccessfulRepairDTO;
import com.fu.flix.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentDAO extends JpaRepository<Comment, Long> {

    void deleteByRequestCodeAndType(String requestCode, String type);

    @Query(value = "SELECT * FROM comments " +
            "WHERE request_code = :requestCode " +
            "AND type = :type", nativeQuery = true)
    Optional<Comment> findComment(String requestCode, String type);

    @Query(value = "SELECT u.full_name as repairerName, avg(c.rating) as rating, r.experience_description as experienceDescription, " +
            "DATE_FORMAT(r.accepted_cv_at,'%d/%m/%Y') as joinAt, r.experience_year as experienceYear, count(c.id) as totalComment " +
            "FROM repairers r " +
            "JOIN users u " +
            "ON r.user_id = u.id " +
            "LEFT JOIN repair_requests_matching rrm " +
            "ON r.user_id = rrm.repairer_id " +
            "LEFT JOIN comments c " +
            "ON c.request_code = rrm.request_code " +
            "AND c.type = 'CUSTOMER_COMMENT' " +
            "WHERE rrm.repairer_id = :repairerId", nativeQuery = true)
    ICustomerGetRepairerDTO findRepairerProfile(Long repairerId);

    @Query(value = "SELECT count(*) as successfulRepair " +
            "FROM repair_requests rr " +
            "JOIN repair_requests_matching rrm " +
            "ON rr.request_code = rrm.request_code " +
            "WHERE rrm.repairer_id = :repairerId " +
            "AND rr.status_id = 'DO'", nativeQuery = true)
    ISuccessfulRepairDTO findSuccessfulRepair(Long repairerId);

    @Query(value = "SELECT rr.user_id as customerId, u.full_name as customerName, c.rating, c.comment, c.created_at as createdAt, avatar.url as customerAvatar " +
            "FROM comments c " +
            "JOIN repair_requests_matching rrm " +
            "ON c.request_code = rrm.request_code " +
            "JOIN repair_requests rr " +
            "ON c.request_code = rr.request_code " +
            "JOIN users u " +
            "ON u.id = rr.user_id " +
            "JOIN images avatar " +
            "ON avatar.id = u.avatar " +
            "WHERE rrm.repairer_id = :repairerId " +
            "AND c.type = 'CUSTOMER_COMMENT' " +
            "ORDER BY c.id DESC " +
            "limit :limit offset :offset", nativeQuery = true)
    List<IRepairerCommentDTO> findRepairComments(Long repairerId, Integer limit, Integer offset);

    @Query(value = "SELECT count(*) " +
            "FROM comments c " +
            "JOIN repair_requests_matching rrm " +
            "ON c.request_code = rrm.request_code " +
            "JOIN repair_requests rr " +
            "ON c.request_code = rr.request_code " +
            "JOIN users u " +
            "ON u.id = rr.user_id " +
            "WHERE rrm.repairer_id = :repairerId " +
            "AND c.type = 'CUSTOMER_COMMENT'", nativeQuery = true)
    long countRepairerComments(Long repairerId);
}
