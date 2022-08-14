package com.fu.flix.dao;

import com.fu.flix.entity.UserUpdateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserUpdateHistoryDAO extends JpaRepository<UserUpdateHistory, Long> {
}
