package com.fu.flix.dao;

import com.fu.flix.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceDAO extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByRepairRequestId(Long repairRequestId);
}
