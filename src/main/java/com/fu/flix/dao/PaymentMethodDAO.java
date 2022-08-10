package com.fu.flix.dao;

import com.fu.flix.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentMethodDAO extends JpaRepository<PaymentMethod, String> {
}
