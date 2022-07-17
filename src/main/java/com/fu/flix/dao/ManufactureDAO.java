package com.fu.flix.dao;

import com.fu.flix.entity.Manufacture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManufactureDAO extends JpaRepository<Manufacture, Long> {
}
