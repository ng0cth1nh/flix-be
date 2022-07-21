package com.fu.flix.dao;

import com.fu.flix.dto.*;
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
            "roles.name as role " +
            "FROM users repairer " +
            "JOIN images avatar " +
            "ON repairer.avatar = avatar.id " +
            "JOIN user_roles ur " +
            "ON ur.user_id = repairer.id " +
            "JOIN roles " +
            "ON roles.id = ur.role_id " +
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

    @Query(value = "SELECT r.user_id as id, avatar.url as avatar, u.full_name as repairerName, u.phone as repairerPhone, " +
            "CASE " +
            "   WHEN u.is_active THEN 'ACTIVE' " +
            "   ELSE 'BAN' " +
            "END as status, " +
            "u.date_of_birth as dateOfBirth, u.gender, u.email, ua.id as addressId, u.created_at as createdAt, r.experience_year as experienceYear, " +
            "r.experience_description as experienceDescription, ic.identity_card_number as identityCardNumber, ic.type as identityCardType, " +
            "front_img.url as frontImage, back_img.url as backSideImage, r.accepted_account_at as acceptedAccountAt, roles.name as role " +
            "FROM users u " +
            "JOIN images avatar " +
            "ON avatar.id = u.avatar " +
            "JOIN user_roles ur " +
            "ON ur.user_id = u.id " +
            "JOIN user_addresses ua " +
            "ON ua.user_id = u.id " +
            "JOIN repairers r " +
            "ON r.user_id = u.id " +
            "JOIN identity_cards ic " +
            "ON ic.repairer_id = r.user_id " +
            "JOIN images front_img " +
            "ON front_img.id = ic.front_image_id " +
            "JOIN images back_img " +
            "ON back_img.id = ic.back_side_image_id " +
            "JOIN roles " +
            "ON roles.id = ur.role_id " +
            "WHERE (ur.role_id = 'R' OR ur.role_id = 'PR' ) " +
            "AND ua.is_main_address " +
            "AND r.user_id = :repairerId", nativeQuery = true)
    Optional<IRepairerDetailDTO> findRepairerDetail(Long repairerId);

    @Query(value = "SELECT u.id as id, avatar.url as avatar, u.full_name as name, u.phone, roles.name as role, " +
            "u.ban_reason as banReason, u.ban_at as banAt " +
            "FROM users u " +
            "JOIN images avatar " +
            "ON u.avatar = avatar.id " +
            "JOIN user_roles ur " +
            "ON ur.user_id = u.id " +
            "JOIN roles " +
            "ON roles.id = ur.role_id " +
            "WHERE (ur.role_id = 'C' or ur.role_id = 'PR' or ur.role_id = 'R') " +
            "AND NOT u.is_active limit :limit offset :offset", nativeQuery = true)
    List<IBanUserDTO> findBanUsers(Integer limit, Integer offset);

    @Query(value = "SELECT repairer.id, repairer.full_name as repairerName, repairer.phone as repairerPhone, repairer.created_at as createdAt " +
            "FROM users repairer " +
            "JOIN user_roles ur " +
            "ON repairer.id = ur.user_id " +
            "WHERE ur.role_id = 'PR' " +
            "limit :limit offset :offset", nativeQuery = true)
    List<IPendingRepairerDTO> findPendingRepairers(Integer limit, Integer offset);

    @Query(value = "SELECT u.full_name as fullName, avatar.url as avatar, u.phone, u.date_of_birth as dateOfBirth, u.gender, u.email, " +
            "r.experience_description as experienceDescription, ic.identity_card_number as identityCardNumber, ua.id addressId, b.balance, role.name as role " +
            "FROM users u " +
            "JOIN images avatar " +
            "ON avatar.id = u.avatar " +
            "JOIN repairers r " +
            "ON r.user_id = u.id " +
            "JOIN user_roles ur " +
            "ON ur.user_id = r.user_id " +
            "JOIN identity_cards ic " +
            "ON ic.repairer_id = r.user_id J" +
            "OIN user_addresses ua " +
            "ON ua.user_id = r.user_id " +
            "JOIN balances b " +
            "ON b.user_id = r.user_id " +
            "JOIN roles role " +
            "ON role.id = ur.role_id " +
            "WHERE (ur.role_id = 'R' OR ur.role_id = 'PR') " +
            "AND u.is_active " +
            "AND ua.deleted_at IS NULL " +
            "AND u.id = :repairerId", nativeQuery = true)
    Optional<IRepairerProfileDTO> findRepairerProfile(Long repairerId);
}
