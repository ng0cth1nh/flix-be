package com.fu.flix.dao;

import com.fu.flix.dto.IDetailFixingRequestDTO;
import com.fu.flix.dto.IRequestingDTO;
import com.fu.flix.entity.RepairRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepairRequestDAO extends JpaRepository<RepairRequest, Long> {
    Optional<RepairRequest> findByRequestCode(String requestCode);

    List<RepairRequest> findByUserIdAndStatusIdOrderByCreatedAtDesc(Long userId, String statusId);

    @Query(value = "SELECT stt.name as status, i_sv.url as serviceImage, sv.id as serviceId, sv.name as serviceName, c_address.id as customerAddressId, customer.phone as customerPhone, customer.full_name as customerName, rr.expect_start_fixing_at as expectFixingDay, rr.description as requestDescription, v.id as voucherId, pm.name as paymentMethod, rr.created_at as createdAt, iv.total_price as price, iv.actual_proceeds as actualPrice, iv.vat_price as vatPrice, rr.request_code as requestCode, r_address.id as repairerAddressId, repairer.phone as repairerPhone, repairer.full_name as repairerName, repairer.id as repairerId, repairer_avatar.url  as repairerAvatar " +
            "FROM repair_requests rr " +
            "JOIN services sv " +
            "ON rr.service_id = sv.id " +
            "JOIN images i_sv " +
            "ON sv.image_id = i_sv.id " +
            "JOIN status stt " +
            "ON rr.status_id = stt.id " +
            "JOIN users customer " +
            "ON customer.id = rr.user_id " +
            "LEFT JOIN vouchers v " +
            "ON rr.voucher_id = v.id " +
            "JOIN payment_methods pm " +
            "ON rr.payment_method_id = pm.id " +
            "JOIN invoices iv " +
            "ON rr.request_code = iv.request_code " +
            "JOIN user_addresses c_address " +
            "ON rr.address_id = c_address.id " +
            "LEFT JOIN repair_requests_matching rrm " +
            "ON rr.request_code = rrm.request_code " +
            "LEFT JOIN users repairer " +
            "ON repairer.id = rrm.repairer_id " +
            "LEFT JOIN user_addresses r_address " +
            "ON r_address.user_id = repairer.id " +
            "LEFT JOIN images repairer_avatar " +
            "ON repairer_avatar.id = repairer.avatar " +
            "WHERE rr.request_code = :requestCode " +
            "AND customer.is_active " +
            "AND customer.id = :customerId", nativeQuery = true)
    IDetailFixingRequestDTO findDetailFixingRequest(Long customerId, String requestCode);

    List<RepairRequest> findByUserIdAndStatusId(Long userId, String statusId);

    Optional<RepairRequest> findByUserIdAndRequestCode(Long userId, String requestCode);

    @Query(value = "SELECT customer.full_name as customerName, avatar.url as avatar, sv.name as serviceName, rr.expect_start_fixing_at as expectFixingTime," +
            " rr.address_id as addressId, rr.description, rr.request_code as requestCode, icon.url as iconImage, rr.created_at as createdAt " +
            "FROM repair_requests rr " +
            "JOIN user_addresses ua " +
            "ON rr.address_id = ua.id " +
            "JOIN communes c " +
            "ON ua.commune_id = c.id " +
            "JOIN districts d " +
            "ON c.district_id = d.id " +
            "JOIN users customer " +
            "ON customer.id = rr.user_id " +
            "JOIN images avatar " +
            "ON avatar.id = customer.avatar " +
            "JOIN services sv " +
            "ON sv.id = rr.service_id " +
            "JOIN  images icon " +
            "ON icon.id = sv.icon_id " +
            "WHERE rr.status_id = 'PE' " +
            "AND rr.service_id in (:serviceIds) " +
            "AND district_id = :districtId", nativeQuery = true)
    List<IRequestingDTO> findPendingRequestByDistrict(List<Long> serviceIds, String districtId);

    @Query(value = "SELECT customer.full_name as customerName, avatar.url as avatar, sv.name as serviceName, rr.expect_start_fixing_at as expectFixingTime," +
            " rr.address_id as addressId, rr.description, rr.request_code as requestCode, icon.url as iconImage, rr.created_at as createdAt " +
            "FROM repair_requests rr " +
            "JOIN user_addresses ua " +
            "ON rr.address_id = ua.id " +
            "JOIN communes c " +
            "ON ua.commune_id = c.id " +
            "JOIN districts d " +
            "ON c.district_id = d.id " +
            "JOIN users customer " +
            "ON customer.id = rr.user_id " +
            "JOIN images avatar " +
            "ON avatar.id = customer.avatar " +
            "JOIN services sv " +
            "ON sv.id = rr.service_id " +
            "JOIN  images icon " +
            "ON icon.id = sv.icon_id " +
            "JOIN cities ct " +
            "ON d.city_id = ct.id " +
            "WHERE rr.status_id = 'PE' " +
            "AND rr.service_id in (:serviceIds) " +
            "AND ct.id = :cityId", nativeQuery = true)
    List<IRequestingDTO> findPendingRequestByCity(List<Long> serviceIds, String cityId);
}
