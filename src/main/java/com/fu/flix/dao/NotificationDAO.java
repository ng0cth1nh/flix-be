package com.fu.flix.dao;

import com.fu.flix.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationDAO extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdAndDeletedAtIsNullOrderByDateDesc(Long userId, Pageable pageable);

    long countByUserIdAndDeletedAtIsNull(Long userId);
    Optional<Notification> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
}
