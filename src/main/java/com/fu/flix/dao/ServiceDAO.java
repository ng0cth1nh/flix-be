package com.fu.flix.dao;

import com.fu.flix.dto.IServiceDTO;
import com.fu.flix.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ServiceDAO extends JpaRepository<Service, Long> {
    List<Service> findByCategoryId(Long categoryId);

    @Query(value = "SELECT sv.id as serviceId, sv.name as serviceName, icon.url as icon, image.url as image " +
            "FROM services sv " +
            "JOIN images icon " +
            "ON sv.icon_id = icon.id " +
            "JOIN images image " +
            "ON sv.image_id = image.id " +
            "WHERE sv.name LIKE %:keyword%", nativeQuery = true)
    List<IServiceDTO> searchServices(String keyword);
}
