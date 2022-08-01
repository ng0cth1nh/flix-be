package com.fu.flix.dao;

import com.fu.flix.dto.IAdminCheckRegisterServiceDTO;
import com.fu.flix.dto.IRegisterServiceProfileDTO;
import com.fu.flix.entity.Repairer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepairerDAO extends JpaRepository<Repairer, Long> {
    Optional<Repairer> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    @Query(value = "SELECT service_icon.url as serviceIcon, sv.name as serviceName, sv.id as serviceId " +
            "FROM repairers r " +
            "JOIN repairer_services rs " +
            "ON r.user_id = rs.user_id " +
            "JOIN services sv " +
            "ON sv.id = rs.service_id " +
            "JOIN images service_icon " +
            "ON service_icon.id = sv.icon_id " +
            "WHERE r.user_id = :repairerId", nativeQuery = true)
    List<IRegisterServiceProfileDTO> findRegisterServicesProfile(Long repairerId);

    @Query(value = "SELECT service_img.url as serviceImage, sv.name as serviceName, sv.id as serviceId " +
            "FROM repairers r " +
            "JOIN repairer_services rs " +
            "ON r.user_id = rs.user_id " +
            "JOIN services sv " +
            "ON sv.id = rs.service_id " +
            "JOIN images service_img " +
            "ON service_img.id = sv.image_id " +
            "WHERE r.user_id = :repairerId", nativeQuery = true)
    List<IAdminCheckRegisterServiceDTO> findRegisterServicesForAdmin(Long repairerId);
}
