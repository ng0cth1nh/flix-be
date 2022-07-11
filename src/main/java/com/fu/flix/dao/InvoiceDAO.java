package com.fu.flix.dao;

import com.fu.flix.dto.IInvoiceDTO;
import com.fu.flix.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceDAO extends JpaRepository<Invoice, String> {
    Optional<Invoice> findByRequestCode(String requestCode);

    @Query(value = "select customer.full_name as customerName, c_avatar.url as customerAvatar, customer.phone as customerPhone, c_ua.id as customerAddressId, " +
            "repairer.full_name as repairerName, r_avatar.url as repairerAvatar, repairer.phone as repairerPhone, r_ua.id as repairerAddressId, " +
            "iv.total_extra_service_price as totalExtraServicePrice, iv.total_accessory_price as totalAccessoryPrice, iv.total_sub_service_price as totalSubServicePrice, " +
            "iv.inspection_price as inspectionPrice, iv.total_discount as totalDiscount, rr.expect_start_fixing_at as expectFixingTime, rr.voucher_id as voucherId, " +
            "pm.name as paymentMethod, rr.request_code as requestCode, rr.created_at as createdAt, iv.actual_proceeds as actualPrice, iv.total_price as totalPrice, " +
            "iv.vat_price as vatPrice, rrm.created_at as approvedTime " +
            "FROM repair_requests rr " +
            "JOIN users customer " +
            "ON rr.user_id = customer.id " +
            "JOIN images c_avatar " +
            "ON customer.avatar = c_avatar.id " +
            "JOIN user_addresses c_ua " +
            "ON c_ua.id = rr.address_id " +
            "JOIN repair_requests_matching rrm " +
            "ON rrm.request_code = rr.request_code " +
            "JOIN users repairer " +
            "ON repairer.id = rrm.repairer_id " +
            "JOIN images r_avatar " +
            "ON repairer.avatar = r_avatar.id " +
            "JOIN user_addresses r_ua " +
            "ON r_ua.user_id = repairer.id " +
            "JOIN invoices iv " +
            "ON iv.request_code = rr.request_code " +
            "LEFT JOIN vouchers voucher " +
            "ON voucher.id = rr.voucher_id " +
            "JOIN payment_methods pm " +
            "ON pm.id = rr.payment_method_id " +
            "WHERE r_ua.is_main_address " +
            "AND (rr.status_id = 'DO' OR rr.status_id = 'PW') " +
            "AND rr.request_code = :requestCode " +
            "AND customer.id = :customerId " +
            "AND customer.is_active", nativeQuery = true)
    Optional<IInvoiceDTO> findCustomerInvoice(String requestCode, Long customerId);

    @Query(value = "select customer.full_name as customerName, c_avatar.url as customerAvatar, customer.phone as customerPhone, c_ua.id as customerAddressId, " +
            "repairer.full_name as repairerName, r_avatar.url as repairerAvatar, repairer.phone as repairerPhone, r_ua.id as repairerAddressId, " +
            "iv.total_extra_service_price as totalExtraServicePrice, iv.total_accessory_price as totalAccessoryPrice, iv.total_sub_service_price as totalSubServicePrice, " +
            "iv.inspection_price as inspectionPrice, iv.total_discount as totalDiscount, rr.expect_start_fixing_at as expectFixingTime, rr.voucher_id as voucherId, " +
            "pm.name as paymentMethod, rr.request_code as requestCode, rr.created_at as createdAt, iv.actual_proceeds as actualPrice, iv.total_price as totalPrice, " +
            "iv.vat_price as vatPrice, rrm.created_at as approvedTime " +
            "FROM repair_requests rr " +
            "JOIN users customer " +
            "ON rr.user_id = customer.id " +
            "JOIN images c_avatar " +
            "ON customer.avatar = c_avatar.id " +
            "JOIN user_addresses c_ua " +
            "ON c_ua.id = rr.address_id " +
            "JOIN repair_requests_matching rrm " +
            "ON rrm.request_code = rr.request_code " +
            "JOIN users repairer " +
            "ON repairer.id = rrm.repairer_id " +
            "JOIN images r_avatar " +
            "ON repairer.avatar = r_avatar.id " +
            "JOIN user_addresses r_ua " +
            "ON r_ua.user_id = repairer.id " +
            "JOIN invoices iv " +
            "ON iv.request_code = rr.request_code " +
            "LEFT JOIN vouchers voucher " +
            "ON voucher.id = rr.voucher_id " +
            "JOIN payment_methods pm " +
            "ON pm.id = rr.payment_method_id " +
            "WHERE r_ua.is_main_address " +
            "AND (rr.status_id = 'DO' OR rr.status_id = 'PW') " +
            "AND rr.request_code = :requestCode " +
            "AND repairer.id = :repairerId " +
            "AND repairer.is_active", nativeQuery = true)
    Optional<IInvoiceDTO> findRepairerInvoice(String requestCode, Long repairerId);
}
