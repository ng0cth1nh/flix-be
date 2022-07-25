package com.fu.flix.dao;

import com.fu.flix.dto.IAdminSearchServiceDTO;
import com.fu.flix.dto.ISearchActiveServiceDTO;
import com.fu.flix.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ServiceDAO extends JpaRepository<Service, Long> {
    List<Service> findByCategoryIdAndIsActive(Long categoryId, Boolean isActive);

    @Query(value = "SELECT sv.id as serviceId, sv.name as serviceName, icon.url as icon, " +
            "image.url as image, sv.inspection_price as price, " +
            "CASE WHEN sv.is_active THEN 'ACTIVE' ELSE 'INACTIVE' END as status " +
            "FROM services sv " +
            "JOIN images icon " +
            "ON sv.icon_id = icon.id " +
            "JOIN images image " +
            "ON sv.image_id = image.id " +
            "WHERE sv.name LIKE %:keyword% " +
            "AND sv.is_active", nativeQuery = true)
    List<ISearchActiveServiceDTO> searchActiveServices(String keyword);

    @Query(value = "SELECT sv.id as serviceId, sv.name as serviceName, icon.url as icon, image.url as image, " +
            "CASE WHEN sv.is_active THEN 'ACTIVE' ELSE 'INACTIVE' END as status " +
            "FROM services sv " +
            "JOIN images icon " +
            "ON sv.icon_id = icon.id " +
            "JOIN images image " +
            "ON sv.image_id = image.id " +
            "WHERE sv.name LIKE %:keyword% ", nativeQuery = true)
    List<IAdminSearchServiceDTO> searchServicesForAdmin(String keyword);
}
