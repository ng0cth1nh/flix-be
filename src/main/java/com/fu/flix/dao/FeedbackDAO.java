package com.fu.flix.dao;

import com.fu.flix.dto.ISearchFeedbackDTO;
import com.fu.flix.entity.Feedback;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackDAO extends JpaRepository<Feedback, Long> {
    List<Feedback> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query(value = "SELECT fb.id, customer.phone, fb.type as feedbackType, fb.title, DATE_FORMAT(fb.created_at, '%Y-%m-%d %H:%i:%s') as createdAt, " +
            "stt.name as status " +
            "FROM feedbacks fb " +
            "JOIN users customer " +
            "ON customer.id = fb.user_id " +
            "JOIN status stt " +
            "ON stt.id = fb.status_id " +
            "WHERE customer.phone LIKE %:phone% " +
            "AND fb.status_id IN (:statusIds) " +
            "AND fb.type IN (:feedbackTypes)", nativeQuery = true)
    List<ISearchFeedbackDTO> searchFeedbackForAdmin(String phone, List<String> statusIds, List<String> feedbackTypes);

    @Query(value = "SELECT count(*) FROM feedbacks WHERE status_id = 'PE'", nativeQuery = true)
    long countPendingFeedbacks();
}
