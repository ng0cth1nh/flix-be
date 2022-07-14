package com.fu.flix.dao;

import com.fu.flix.dto.ICustomerDTO;
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
}
