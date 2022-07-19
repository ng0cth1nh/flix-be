package com.fu.flix.dao;

import com.fu.flix.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificateDAO extends JpaRepository<Certificate, Long> {
    List<Certificate> findByRepairerId(Long repairerId);
}
