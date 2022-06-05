package com.fu.flix.dao;

import com.fu.flix.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DistrictDAO extends JpaRepository<District, String> {
}
