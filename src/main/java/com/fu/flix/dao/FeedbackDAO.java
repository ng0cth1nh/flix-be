package com.fu.flix.dao;

import com.fu.flix.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackDAO extends JpaRepository<Feedback, Long> {
}
