package com.fu.flix.dao;

import com.fu.flix.entity.User;
import com.fu.flix.entity.UserAddress;
import com.fu.flix.entity.UserAddressId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAddressDAO extends JpaRepository<UserAddress, UserAddressId> {
    Optional<UserAddress> findByUserAndIsMainAddress(User user, boolean isMainAddress);
}
