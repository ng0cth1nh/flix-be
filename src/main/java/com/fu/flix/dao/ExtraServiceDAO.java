package com.fu.flix.dao;

import com.fu.flix.entity.ExtraService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExtraServiceDAO extends JpaRepository<ExtraService, Long> {
    void deleteAllByRequestCode(String requestCode);

    List<ExtraService> findByRequestCode(String requestCode);
}
