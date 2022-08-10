package com.fu.flix.dao;

import com.fu.flix.entity.FeedbackResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackResponseDAO extends JpaRepository<FeedbackResponse, Long> {
    List<FeedbackResponse> findByFeedbackIdOrderByCreatedAtDesc(Long feedbackId);
}
