package com.fu.flix.dao;

import com.fu.flix.entity.UserAddress;
import com.fu.flix.entity.UserAddressId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAddressDAO extends JpaRepository<UserAddress, UserAddressId> {
}
