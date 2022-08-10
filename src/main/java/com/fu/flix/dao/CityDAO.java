package com.fu.flix.dao;

import com.fu.flix.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityDAO extends JpaRepository<City, String> {
}
