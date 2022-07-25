package com.fu.flix.dao;

import com.fu.flix.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificateDAO extends JpaRepository<Certificate, Long> {
}
