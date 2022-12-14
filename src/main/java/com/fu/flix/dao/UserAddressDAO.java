package com.fu.flix.dao;

import com.fu.flix.dto.IAddressDTO;
import com.fu.flix.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserAddressDAO extends JpaRepository<UserAddress, Long> {
    void deleteAllByUserId(Long userId);

    Optional<UserAddress> findByUserIdAndIsMainAddressAndDeletedAtIsNull(Long userId, boolean isMainAddress);

    @Query(value = "SELECT ua.id as addressId, CONCAT(ua.street_address, ', ', commune.name, ', ', district.name, ', ', city.name) as addressName, " +
            "ua.name as customerName, ua.phone as customerPhone, " +
            "CASE WHEN ua.is_main_address THEN 'true' ELSE 'false' END as isMainAddress, district.id as districtId, city.id as cityId, " +
            "commune.id as communeId " +
            "FROM user_addresses ua " +
            "JOIN communes commune " +
            "ON commune.id = ua.commune_id " +
            "JOIN districts district " +
            "ON district.id = commune.district_id " +
            "JOIN cities city " +
            "ON city.id = district.city_id " +
            "WHERE ua.deleted_at IS NULL " +
            "AND ua.user_id = :userId", nativeQuery = true)
    List<IAddressDTO> findByUserIdAndDeletedAtIsNull(Long userId);

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

    @Query(value = "SELECT d.id as districtId " +
            "FROM districts d " +
            "JOIN communes c " +
            "ON c.district_id = d.id " +
            "JOIN user_addresses ua " +
            "ON ua.commune_id = c.id " +
            "WHERE ua.id = :userAddressId " +
            "AND ua.deleted_at IS NULL", nativeQuery = true)
    String findDistrictIdByUserAddressId(Long userAddressId);

    @Query(value = "SELECT ct.id as cityId " +
            "FROM districts d " +
            "JOIN communes c " +
            "ON c.district_id = d.id " +
            "JOIN user_addresses ua " +
            "ON ua.commune_id = c.id " +
            "JOIN cities ct ON ct.id = d.city_id " +
            "WHERE ua.id = :userAddressId " +
            "AND ua.deleted_at IS NULL", nativeQuery = true)
    String findCityIdByUserAddressId(Long userAddressId);
}
