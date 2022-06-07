package com.fu.flix.dao;

import com.fu.flix.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAddressDAO extends JpaRepository<UserAddress, Long> {
    Optional<UserAddress> findByUserIdAndIsMainAddress(Long userId, boolean isMainAddress);

    List<UserAddress> findByUserId(Long userId);
}
