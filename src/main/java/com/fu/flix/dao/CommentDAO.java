package com.fu.flix.dao;

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
}
