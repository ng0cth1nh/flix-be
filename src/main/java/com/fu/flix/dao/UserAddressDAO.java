package com.fu.flix.dao;

import com.fu.flix.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserAddressDAO extends JpaRepository<UserAddress, Long> {
    Optional<UserAddress> findByUserIdAndIsMainAddressAndDeletedAtIsNull(Long userId, boolean isMainAddress);

    List<UserAddress> findByUserIdAndDeletedAtIsNull(Long userId);

    @Query(value = "SELECT * FROM user_addresses " +
            "WHERE user_id = :userId " +
            "AND id = :id " +
            "AND is_main_address <> true " +
            "AND deleted_at IS NULL", nativeQuery = true)
    Optional<UserAddress> findUserAddressToDelete(Long userId, Long id);

    @Query(value = "SELECT * FROM user_addresses " +
            "WHERE user_id = :userId " +
            "AND id = :id " +
            "AND deleted_at IS NULL", nativeQuery = true)
    Optional<UserAddress> findUserAddressToEdit(Long userId, Long id);
}
