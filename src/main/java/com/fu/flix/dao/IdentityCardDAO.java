package com.fu.flix.dao;

import com.fu.flix.entity.IdentityCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdentityCardDAO extends JpaRepository<IdentityCard, String> {
    Optional<IdentityCard> findByIdentityCardNumber(String cardNumber);
}
