package com.fu.flix.dao;

import com.fu.flix.dto.*;
import com.fu.flix.entity.RepairRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepairRequestDAO extends JpaRepository<RepairRequest, Long> {
    Optional<RepairRequest> findByRequestCode(String requestCode);

    List<RepairRequest> findByUserIdAndStatusIdOrderByCreatedAtDesc(Long userId, String statusId);

    @Query(value = "SELECT stt.name as status, i_sv.url as serviceImage, sv.id as serviceId, sv.name as serviceName, iv.customer_address as customerAddress," +
            " iv.customer_phone as customerPhone, iv.customer_name as customerName, rr.expect_start_fixing_at as expectFixingDay, rr.description as requestDescription, " +
            "v.id as voucherId, pm.name as paymentMethod, rr.created_at as createdAt, iv.total_price as totalPrice, iv.actual_proceeds as actualPrice, " +
            "iv.vat_price as vatPrice, rr.request_code as requestCode, iv.repairer_address as repairerAddress, iv.repairer_phone as repairerPhone, " +
            "iv.repairer_name as repairerName, repairer.id as repairerId, repairer_avatar.url  as repairerAvatar, iv.inspection_price as inspectionPrice, " +
            "iv.total_discount as totalDiscount, rrm.created_at as approvedTime " +
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
            "LEFT JOIN invoices iv " +
            "ON rr.request_code = iv.request_code " +
            "LEFT JOIN repair_requests_matching rrm " +
            "ON rr.request_code = rrm.request_code " +
            "LEFT JOIN users repairer " +
            "ON repairer.id = rrm.repairer_id " +
            "LEFT JOIN images repairer_avatar " +
            "ON repairer_avatar.id = repairer.avatar " +
            "WHERE rr.request_code = :requestCode " +
            "AND customer.is_active " +
            "AND customer.id = :customerId", nativeQuery = true)
    IDetailFixingRequestForCustomerDTO findDetailFixingRequestForCustomer(Long customerId, String requestCode);

    @Query(value = "SELECT stt.name as status, sv_img.url as serviceImage, sv.id as serviceId, sv.name as serviceName, customer.id as customerId," +
            " avatar.url as avatar, iv.customer_address as address, iv.customer_phone as customerPhone, iv.customer_name as customerName, rr.expect_start_fixing_at as expectFixingTime," +
            " rr.description as requestDescription, v.id as voucherId, pm.name as paymentMethod, rr.created_at as createdAt, iv.total_price as totalPrice," +
            " iv.actual_proceeds as actualPrice, iv.vat_price as vatPrice, rr.request_code as requestCode, iv.inspection_price as inspectionPrice," +
            "iv.total_discount as totalDiscount, rrm.created_at as approvedTime " +
            "FROM repair_requests rr " +
            "LEFT JOIN repair_requests_matching rrm " +
            "ON rr.request_code = rrm.request_code " +
            "JOIN status stt " +
            "ON rr.status_id = stt.id " +
            "JOIN services sv " +
            "ON sv.id = rr.service_id " +
            "JOIN images sv_img " +
            "ON sv_img.id = sv.image_id " +
            "JOIN users customer " +
            "ON customer.id = rr.user_id " +
            "JOIN images avatar " +
            "ON avatar.id = customer.avatar " +
            "LEFT JOIN vouchers v " +
            "ON rr.voucher_id = v.id " +
            "JOIN payment_methods pm " +
            "ON rr.payment_method_id = pm.id " +
            "LEFT JOIN invoices iv " +
            "ON iv.request_code = rr.request_code " +
            "WHERE rr.request_code = :requestCode " +
            "AND (" +
            "   CASE WHEN rr.status_id <> 'PE' " +
            "   THEN rrm.repairer_id = :repairerId " +
            "   ELSE TRUE" +
            "   END" +
            "    )", nativeQuery = true)
    IDetailFixingRequestForRepairerDTO findDetailFixingRequestForRepairer(Long repairerId, String requestCode);

    List<RepairRequest> findByUserIdAndStatusId(Long userId, String statusId);

    Optional<RepairRequest> findByUserIdAndRequestCode(Long userId, String requestCode);

    @Query(value = "SELECT iv.customer_name as customerName, avatar.url as avatar, sv.name as serviceName, rr.expect_start_fixing_at as expectFixingTime, " +
            "iv.customer_address as address, rr.description, rr.request_code as requestCode, icon.url as iconImage, rr.created_at as createdAt " +
            "FROM repair_requests rr " +
            "LEFT JOIN invoices iv " +
            "ON iv.request_code = rr.request_code " +
            "JOIN communes c " +
            "ON iv.commune_id = c.id " +
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

    @Query(value = "SELECT iv.customer_name as customerName, avatar.url as avatar, sv.name as serviceName, rr.expect_start_fixing_at as expectFixingTime," +
            " iv.customer_address as address, rr.description, rr.request_code as requestCode, icon.url as iconImage, rr.created_at as createdAt " +
            "FROM repair_requests rr " +
            "LEFT JOIN invoices iv " +
            "ON iv.request_code = rr.request_code " +
            "JOIN communes c " +
            "ON iv.commune_id = c.id " +
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

    @Query(value = "SELECT iv.customer_name as customerName, avatar.url as avatar, sv.name as serviceName, rr.expect_start_fixing_at as expectFixingTime," +
            " iv.customer_address as address, rr.description, rr.request_code as requestCode, icon.url as iconImage, rr.created_at as createdAt " +
            "FROM repair_requests rr " +
            "LEFT JOIN invoices iv " +
            "ON iv.request_code = rr.request_code " +
            "JOIN communes c " +
            "ON iv.commune_id = c.id " +
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
            "AND c.id = :communeId", nativeQuery = true)
    List<IRequestingDTO> findPendingRequestByCommune(List<Long> serviceIds, String communeId);

    @Query(value = "SELECT iv.customer_name as customerName, avatar.url as avatar, sv.name as serviceName, rr.expect_start_fixing_at as expectFixingTime, " +
            "iv.customer_address as address, rr.description, rr.request_code as requestCode, icon.url as iconImage, rr.created_at as createdAt " +
            "FROM repair_requests rr " +
            "LEFT JOIN invoices iv " +
            "ON iv.request_code = rr.request_code " +
            "JOIN communes c " +
            "ON iv.commune_id = c.id " +
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
            "AND c.id = :communeId " +
            "AND rr.expect_start_fixing_at >= :start AND rr.expect_start_fixing_at <= :end", nativeQuery = true)
    List<IRequestingDTO> filterPendingRequestByCommune(List<Long> serviceIds, String communeId, LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT iv.customer_name as customerName, avatar.url as avatar, sv.name as serviceName, rr.expect_start_fixing_at as expectFixingTime, " +
            "iv.customer_address as address, rr.description, rr.request_code as requestCode, icon.url as iconImage, rr.created_at as createdAt " +
            "FROM repair_requests rr " +
            "LEFT JOIN invoices iv " +
            "ON iv.request_code = rr.request_code " +
            "JOIN communes c " +
            "ON iv.commune_id = c.id " +
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
            "AND ct.id = :cityId " +
            "AND rr.expect_start_fixing_at >= :start AND rr.expect_start_fixing_at <= :end", nativeQuery = true)
    List<IRequestingDTO> filterPendingRequestByCity(List<Long> serviceIds, String cityId, LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT iv.customer_name as customerName, avatar.url as avatar, sv.name as serviceName, rr.expect_start_fixing_at as expectFixingTime, " +
            "iv.customer_address as address, rr.description, rr.request_code as requestCode, icon.url as iconImage, rr.created_at as createdAt " +
            "FROM repair_requests rr " +
            "LEFT JOIN invoices iv " +
            "ON iv.request_code = rr.request_code " +
            "JOIN communes c " +
            "ON iv.commune_id = c.id " +
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
            "AND district_id = :districtId " +
            "AND rr.expect_start_fixing_at >= :start AND rr.expect_start_fixing_at <= :end", nativeQuery = true)
    List<IRequestingDTO> filterPendingRequestByDistrict(List<Long> serviceIds, String districtId, LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT * " +
            "FROM (SELECT ROW_NUMBER() OVER (ORDER BY createdAt DESC) row_num, tb1.* " +
            "FROM (SELECT rr.request_code as requestCode, customer.id as customerId, iv.customer_name as customerName, iv.customer_phone as customerPhone, " +
            "repairer.id as repairerId, iv.repairer_name as repairerName, iv.repairer_phone as repairerPhone, stt.name as status, rr.created_at as createdAt " +
            "FROM repair_requests rr " +
            "LEFT JOIN invoices iv " +
            "ON iv.request_code = rr.request_code " +
            "JOIN users customer " +
            "ON rr.user_id = customer.id " +
            "LEFT JOIN repair_requests_matching rrm " +
            "ON rr.request_code = rrm.request_code " +
            "LEFT JOIN users repairer " +
            "ON repairer.id = rrm.repairer_id " +
            "JOIN status stt " +
            "ON stt.id = rr.status_id) AS tb1 ) " +
            "AS tb2 " +
            "WHERE row_num BETWEEN :start and :end", nativeQuery = true)
    List<IRequestInfoDTO> findRequestForAdmin(Integer start, Integer end);

    @Query(value = "SELECT rr.request_code as requestCode, customer.id as customerId, iv.customer_name as customerName, iv.customer_phone as customerPhone, " +
            "repairer.id as repairerId, iv.repairer_name as repairerName, iv.repairer_phone as repairerPhone, stt.name as status, rr.created_at as createdAt " +
            "FROM repair_requests rr " +
            "LEFT JOIN invoices iv " +
            "ON iv.request_code = rr.request_code " +
            "JOIN users customer " +
            "ON rr.user_id = customer.id " +
            "LEFT JOIN repair_requests_matching rrm " +
            "ON rr.request_code = rrm.request_code " +
            "LEFT JOIN users repairer " +
            "ON repairer.id = rrm.repairer_id " +
            "JOIN status stt " +
            "ON stt.id = rr.status_id " +
            "WHERE rr.request_code LIKE %:keyword% " +
            "AND (CASE " +
            "       WHEN :status IS NOT NULL " +
            "       THEN stt.name = :status " +
            "       ELSE true " +
            "    END)", nativeQuery = true)
    List<IRequestInfoDTO> searchRequestForAdmin(String keyword, String status);

    @Query(value = "SELECT rr.request_code as requestCode, iv.customer_name as customerName, iv.customer_phone as customerPhone, " +
            "iv.repairer_name as repairerName, iv.repairer_phone as repairerPhone, stt.name as status, iv.customer_address as customerAddress, " +
            "rr.description, sv.name as serviceName, voucher.id as voucherId, rr.expect_start_fixing_at as expectedFixingTime, pm.name as paymentMethod, " +
            "rr.reason_cancel as cancelReason,  rr.created_at as createdAt, iv.total_price as totalPrice, iv.vat_price as vatPrice, iv.actual_proceeds as actualPrice, " +
            "iv.total_discount as totalDiscount, iv.inspection_price as inspectionPrice, iv.total_sub_service_price as totalSubServicePrice, " +
            "iv.total_accessory_price as totalAccessoryPrice, iv.total_extra_service_price as totalExtraServicePrice " +
            "FROM repair_requests rr " +
            "JOIN users customer " +
            "ON customer.id = rr.user_id " +
            "LEFT JOIN repair_requests_matching rrm " +
            "ON rrm.request_code = rr.request_code " +
            "LEFT JOIN users repairer " +
            "ON repairer.id = rrm.repairer_id " +
            "JOIN status stt " +
            "ON stt.id = rr.status_id " +
            "JOIN services sv " +
            "ON sv.id = rr.service_id " +
            "LEFT JOIN vouchers voucher " +
            "ON rr.voucher_id = voucher.id " +
            "LEFT JOIN invoices iv  " +
            "ON iv.request_code = rr.request_code " +
            "JOIN payment_methods pm " +
            "ON pm.id = rr.payment_method_id " +
            "WHERE rr.request_code = :requestCode", nativeQuery = true)
    Optional<IDetailRequestDTO> findRequestDetailForAdmin(String requestCode);

    @Query(value = "SELECT * " +
            "FROM repair_requests " +
            "WHERE TIMESTAMPDIFF(SECOND, CONVERT_TZ(NOW(),'+00:00','+07:00'), expect_start_fixing_at) <= 3600 " +
            "AND TIMESTAMPDIFF(SECOND, CONVERT_TZ(NOW(),'+00:00','+07:00'), expect_start_fixing_at) >= 0 " +
            "AND status_id IN ('PE');", nativeQuery = true)
    List<RepairRequest> findCancelablePendingRequest();

    @Query(value = "SELECT * " +
            "FROM repair_requests " +
            "WHERE TIMESTAMPDIFF(MINUTE, expect_start_fixing_at, CONVERT_TZ(NOW(),'+00:00','+07:00')) < 5 " +
            "AND TIMESTAMPDIFF(MINUTE, expect_start_fixing_at, CONVERT_TZ(NOW(),'+00:00','+07:00')) >= 0 " +
            "AND status_id IN ('AP');", nativeQuery = true)
    List<RepairRequest> findCancelableApprovalRequest();

    @Query(value = "SELECT * " +
            "FROM repair_requests rr " +
            "JOIN invoices iv " +
            "ON iv.request_code = rr.request_code " +
            "WHERE rr.status_id IN ('FX') " +
            "AND TIMESTAMPDIFF(SECOND,iv.confirm_fixing_at, CONVERT_TZ(NOW(),'+00:00','+07:00')) >= 259200", nativeQuery = true)
    List<RepairRequest> findCancelableFixingRequest();

    @Query(value = "SELECT * FROM repair_requests rr " +
            "WHERE rr.status_id IN ('AP') " +
            "AND TIMESTAMPDIFF(SECOND, CONVERT_TZ(NOW(),'+00:00','+07:00'), expect_start_fixing_at) <= 21600 " +
            "AND TIMESTAMPDIFF(SECOND, CONVERT_TZ(NOW(),'+00:00','+07:00'), expect_start_fixing_at) > 3600", nativeQuery = true)
    List<RepairRequest> findRequestToRemindExpectedFixingTimeDeadline();

    @Query(value = "SELECT * " +
            "FROM repair_requests rr " +
            "JOIN invoices iv " +
            "ON iv.request_code = rr.request_code " +
            "WHERE rr.status_id IN ('FX') " +
            "AND TIMESTAMPDIFF(SECOND,iv.confirm_fixing_at, CONVERT_TZ(NOW(),'+00:00','+07:00')) < 259200 " +
            "AND TIMESTAMPDIFF(SECOND,iv.confirm_fixing_at, CONVERT_TZ(NOW(),'+00:00','+07:00')) >= 0;", nativeQuery = true)
    List<RepairRequest> findRequestToRemindFixingTask();

    @Query(value = "SELECT count(*) " +
            "FROM repair_requests rr " +
            "JOIN repair_requests_matching rrm " +
            "ON rr.request_code = rrm.request_code " +
            "WHERE rrm.repairer_id = :repairerId " +
            "AND rr.status_id NOT IN ('CC', 'DO');", nativeQuery = true)
    long countNumberOfNeededFixingRequest(Long repairerId);
}
