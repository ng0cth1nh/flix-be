package com.fu.flix.dao;

import com.fu.flix.entity.Commune;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommuneDAO extends JpaRepository<Commune, String> {
}
