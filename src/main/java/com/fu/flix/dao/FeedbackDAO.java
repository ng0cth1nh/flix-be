package com.fu.flix.dao;

import com.fu.flix.entity.Feedback;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackDAO extends JpaRepository<Feedback, Long> {
    List<Feedback> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
