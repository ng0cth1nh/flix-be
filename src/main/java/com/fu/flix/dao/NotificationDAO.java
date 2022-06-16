package com.fu.flix.dao;

import com.fu.flix.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationDAO extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdAndDeletedAtIsNull(Long userId);
}
