package com.fu.flix.dao;

import com.fu.flix.dto.IBanUserDTO;
import com.fu.flix.dto.ICustomerDTO;
import com.fu.flix.dto.ICustomerDetailDTO;
import com.fu.flix.dto.IRepairerDTO;
import com.fu.flix.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDAO extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query(value = "SELECT customer.id as id, avatar.url as avatar, customer.full_name as customerName, customer.phone as customerPhone, " +
            "CASE " +
            "   WHEN customer.is_active THEN 'ACTIVE' " +
            "   ELSE 'BAN' " +
            "END as status " +
            "FROM users customer " +
            "JOIN images avatar " +
            "ON customer.avatar = avatar.id " +
            "JOIN user_roles ur " +
            "ON ur.user_id = customer.id " +
            "WHERE ur.role_id = 'C' limit :limit offset :offset", nativeQuery = true)
    List<ICustomerDTO> findCustomersForAdmin(Integer limit, Integer offset);

    @Query(value = "SELECT repairer.id as id, avatar.url as avatar, repairer.full_name as repairerName, repairer.phone as repairerPhone, " +
            "CASE " +
            "   WHEN repairer.is_active THEN 'ACTIVE' " +
            "   ELSE 'BAN' " +
            "END as status, " +
            "CASE " +
            "   WHEN ur.role_id = 'R' THEN 'ROLE_REPAIRER' " +
            "   ELSE 'ROLE_PENDING_REPAIRER' " +
            "END as role " +
            "FROM users repairer " +
            "JOIN images avatar " +
            "ON repairer.avatar = avatar.id " +
            "JOIN user_roles ur " +
            "ON ur.user_id = repairer.id " +
            "WHERE (ur.role_id = 'R' or ur.role_id = 'PR') limit :limit offset :offset", nativeQuery = true)
    List<IRepairerDTO> findRepairersForAdmin(Integer limit, Integer offset);

    @Query(value = "SELECT avatar.url as avatar, customer.full_name as customerName, customer.phone as customerPhone, " +
            "CASE " +
            "   WHEN customer.is_active THEN 'ACTIVE' " +
            "   ELSE 'BAN' " +
            "END as status, customer.date_of_birth as dateOfBirth, customer.gender, customer.email, ua.id as addressId, customer.created_at as createdAt " +
            "FROM users customer " +
            "JOIN images avatar " +
            "ON customer.avatar = avatar.id " +
            "JOIN user_roles ur " +
            "ON ur.user_id = customer.id " +
            "JOIN user_addresses ua " +
            "ON ua.user_id = customer.id " +
            "WHERE ur.role_id = 'C' " +
            "AND ua.is_main_address " +
            "AND customer.id = :customerId", nativeQuery = true)
    Optional<ICustomerDetailDTO> findCustomerDetail(Long customerId);

    @Query(value = "SELECT u.id as id, avatar.url as avatar, u.full_name as name, u.phone, " +
            "CASE " +
            "   WHEN ur.role_id = 'C' THEN 'ROLE_CUSTOMER' " +
            "   WHEN ur.role_id = 'R' THEN 'ROLE_REPAIRER' " +
            "   ELSE 'ROLE_PENDING_REPAIRER' END as role, " +
            "u.ban_reason as banReason, u.ban_at as banAt " +
            "FROM users u " +
            "JOIN images avatar " +
            "ON u.avatar = avatar.id " +
            "JOIN user_roles ur " +
            "ON ur.user_id = u.id " +
            "WHERE (ur.role_id = 'C' or ur.role_id = 'PR' or ur.role_id = 'R') " +
            "AND NOT u.is_active limit :limit offset :offset", nativeQuery = true)
    List<IBanUserDTO> findBanUsers(Integer limit, Integer offset);
}
