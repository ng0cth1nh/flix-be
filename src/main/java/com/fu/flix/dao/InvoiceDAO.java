package com.fu.flix.dao;

import com.fu.flix.dto.IInvoiceDTO;
import com.fu.flix.dto.IStatisticalTransactionDTO;
import com.fu.flix.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface InvoiceDAO extends JpaRepository<Invoice, String> {
    Optional<Invoice> findByRequestCode(String requestCode);

    @Query(value = "select iv.customer_name as customerName, c_avatar.url as customerAvatar, iv.customer_phone as customerPhone, iv.customer_address as customerAddress, " +
            "iv.repairer_name as repairerName, r_avatar.url as repairerAvatar, iv.repairer_phone as repairerPhone, iv.repairer_address as repairerAddress, " +
            "iv.total_extra_service_price as totalExtraServicePrice, iv.total_accessory_price as totalAccessoryPrice, iv.total_sub_service_price as totalSubServicePrice, " +
            "iv.inspection_price as inspectionPrice, iv.total_discount as totalDiscount, rr.expect_start_fixing_at as expectFixingTime, rr.voucher_id as voucherId, " +
            "pm.name as paymentMethod, rr.request_code as requestCode, rr.created_at as createdAt, iv.actual_proceeds as actualPrice, iv.total_price as totalPrice, " +
            "iv.vat_price as vatPrice, rrm.created_at as approvedTime, sv.name as serviceName, sv_img.url as serviceImage, sv.id as serviceId, stt.name as status, " +
            "CASE " +
            "   WHEN cus_cmt.id IS NOT NULL THEN 'true' " +
            "   ELSE 'false' " +
            "END as isCustomerCommented, " +
            "CASE " +
            "   WHEN re_cmt.id IS NOT NULL THEN 'true' " +
            "   ELSE 'false' " +
            "END as isRepairerCommented " +
            "FROM repair_requests rr " +
            "JOIN users customer " +
            "ON rr.user_id = customer.id " +
            "JOIN images c_avatar " +
            "ON customer.avatar = c_avatar.id " +
            "JOIN repair_requests_matching rrm " +
            "ON rrm.request_code = rr.request_code " +
            "JOIN users repairer " +
            "ON repairer.id = rrm.repairer_id " +
            "JOIN images r_avatar " +
            "ON repairer.avatar = r_avatar.id " +
            "JOIN invoices iv " +
            "ON iv.request_code = rr.request_code " +
            "LEFT JOIN vouchers voucher " +
            "ON voucher.id = rr.voucher_id " +
            "JOIN payment_methods pm " +
            "ON pm.id = rr.payment_method_id " +
            "JOIN services sv " +
            "ON sv.id = rr.service_id " +
            "JOIN images sv_img " +
            "ON sv_img.id = sv.image_id " +
            "JOIN status stt " +
            "ON stt.id = rr.status_id " +
            "LEFT JOIN comments cus_cmt " +
            "ON (cus_cmt.request_code = rr.request_code AND cus_cmt.type = 'CUSTOMER_COMMENT') " +
            "LEFT JOIN comments re_cmt " +
            "ON (re_cmt.request_code = rr.request_code AND re_cmt.type = 'REPAIRER_COMMENT') " +
            "WHERE (rr.status_id = 'DO' OR rr.status_id = 'PW') " +
            "AND rr.request_code = :requestCode " +
            "AND customer.id = :customerId " +
            "AND customer.is_active", nativeQuery = true)
    Optional<IInvoiceDTO> findCustomerInvoice(String requestCode, Long customerId);

    @Query(value = "select iv.customer_name as customerName, c_avatar.url as customerAvatar, iv.customer_phone as customerPhone, iv.customer_address as customerAddress, " +
            "iv.repairer_name as repairerName, r_avatar.url as repairerAvatar, iv.repairer_phone as repairerPhone, iv.repairer_address as repairerAddress, " +
            "iv.total_extra_service_price as totalExtraServicePrice, iv.total_accessory_price as totalAccessoryPrice, iv.total_sub_service_price as totalSubServicePrice, " +
            "iv.inspection_price as inspectionPrice, iv.total_discount as totalDiscount, rr.expect_start_fixing_at as expectFixingTime, rr.voucher_id as voucherId, " +
            "pm.name as paymentMethod, rr.request_code as requestCode, rr.created_at as createdAt, iv.actual_proceeds as actualPrice, iv.total_price as totalPrice, " +
            "iv.vat_price as vatPrice, rrm.created_at as approvedTime, sv.name as serviceName, sv_img.url as serviceImage, sv.id as serviceId, stt.name as status, " +
            "CASE " +
            "   WHEN cus_cmt.id IS NOT NULL THEN 'true' " +
            "   ELSE 'false' " +
            "END as isCustomerCommented, " +
            "CASE " +
            "   WHEN re_cmt.id IS NOT NULL THEN 'true' " +
            "   ELSE 'false' " +
            "END as isRepairerCommented " +
            "FROM repair_requests rr " +
            "JOIN users customer " +
            "ON rr.user_id = customer.id " +
            "JOIN images c_avatar " +
            "ON customer.avatar = c_avatar.id " +
            "JOIN repair_requests_matching rrm " +
            "ON rrm.request_code = rr.request_code " +
            "JOIN users repairer " +
            "ON repairer.id = rrm.repairer_id " +
            "JOIN images r_avatar " +
            "ON repairer.avatar = r_avatar.id " +
            "JOIN invoices iv " +
            "ON iv.request_code = rr.request_code " +
            "LEFT JOIN vouchers voucher " +
            "ON voucher.id = rr.voucher_id " +
            "JOIN payment_methods pm " +
            "ON pm.id = rr.payment_method_id " +
            "JOIN services sv " +
            "ON sv.id = rr.service_id " +
            "JOIN images sv_img " +
            "ON sv_img.id = sv.image_id " +
            "JOIN status stt " +
            "ON stt.id = rr.status_id " +
            "LEFT JOIN comments cus_cmt " +
            "ON (cus_cmt.request_code = rr.request_code AND cus_cmt.type = 'CUSTOMER_COMMENT') " +
            "LEFT JOIN comments re_cmt " +
            "ON (re_cmt.request_code = rr.request_code AND re_cmt.type = 'REPAIRER_COMMENT') " +
            "WHERE (rr.status_id = 'DO' OR rr.status_id = 'PW') " +
            "AND rr.request_code = :requestCode " +
            "AND repairer.id = :repairerId " +
            "AND repairer.is_active", nativeQuery = true)
    Optional<IInvoiceDTO> findRepairerInvoice(String requestCode, Long repairerId);

    @Query(value = "SELECT COALESCE(sum(profit), 0) as totalProfit, COALESCE(sum(actual_proceeds), 0) as totalRevenue " +
            "FROM invoices " +
            "WHERE done_at IS NOT NULL " +
            "AND done_at >= :start " +
            "AND done_at < :end", nativeQuery = true)
    IStatisticalTransactionDTO findStatisticalTransactionDTO(LocalDateTime start, LocalDateTime end);
}
