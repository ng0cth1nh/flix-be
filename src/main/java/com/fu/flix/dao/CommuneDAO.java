package com.fu.flix.dao;

import com.fu.flix.entity.Commune;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommuneDAO extends JpaRepository<Commune, String> {
    List<Commune> findByDistrictId(String districtId);
}
