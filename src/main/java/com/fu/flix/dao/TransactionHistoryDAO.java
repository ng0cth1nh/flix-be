package com.fu.flix.dao;

import com.fu.flix.dto.*;
import com.fu.flix.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionHistoryDAO extends JpaRepository<TransactionHistory, Long> {
    @Query(value = "SELECT th.id, th.transaction_code as transactionCode, th.status as status, th.amount, th.type as transactionType, u.full_name as fullName, " +
            "u.phone, DATE_FORMAT(th.created_at, '%Y-%m-%d %H:%i:%s') as payDate, th.id as transactionId " +
            "FROM transaction_histories th " +
            "LEFT JOIN vnpay_transactions vt " +
            "ON vt.id = th.vnpay_transaction_id " +
            "LEFT JOIN users u " +
            "ON u.id = th.user_id " +
            "ORDER BY th.created_at DESC " +
            "limit :limit offset :offset", nativeQuery = true)
    List<ITransactionDTO> findTransactionsForAdmin(Integer limit, Integer offset);

    @Query(value = "SELECT th.id, th.transaction_code as transactionCode, vt.transaction_no as vnpTransactionNo, th.amount, th.type as transactionType, u.full_name as fullName, " +
            "u.phone, DATE_FORMAT(th.created_at, '%Y-%m-%d %H:%i:%s') as payDate, " +
            "CASE " +
            "   WHEN wr.id IS NOT NULL " +
            "   THEN wr.bank_code " +
            "   ELSE vt.bank_code " +
            "END as bankCode, " +
            "vt.card_type as cardType, " +
            "vt.order_info as orderInfo, vt.bank_tran_no as vnpBankTranNo, th.status as status, th.fail_reason as failReason " +
            "FROM transaction_histories th " +
            "LEFT JOIN vnpay_transactions vt " +
            "ON vt.id = th.vnpay_transaction_id " +
            "LEFT JOIN users u " +
            "ON u.id = th.user_id " +
            "LEFT JOIN withdraw_requests wr " +
            "ON wr.id = th.withdraw_request_id " +
            "WHERE th.id = :id", nativeQuery = true)
    Optional<ITransactionDetailDTO> findTransactionDetail(Long id);

    @Query(value = "SELECT th.id, th.transaction_code as transactionCode, th.status as status, th.amount, th.type as transactionType, u.full_name as fullName, " +
            "u.phone, DATE_FORMAT(th.created_at, '%Y-%m-%d %H:%i:%s') as payDate, th.id as transactionId " +
            "FROM transaction_histories th " +
            "LEFT JOIN vnpay_transactions vt " +
            "ON vt.id = th.vnpay_transaction_id " +
            "LEFT JOIN users u " +
            "ON u.id = th.user_id " +
            "WHERE th.transaction_code LIKE %:keyword% " +
            "AND th.type IN (:transactionTypes) " +
            "AND th.status IN (:transactionStatus) " +
            "ORDER BY th.created_at DESC", nativeQuery = true)
    List<ITransactionDTO> searchTransactionsForAdmin(String keyword, List<String> transactionTypes, List<String> transactionStatus);

    Optional<TransactionHistory> findByIdAndTypeAndStatus(Long id, String type, String status);

    @Query(value = "SELECT * FROM transaction_histories " +
            "WHERE user_id = :repairerId " +
            "ORDER BY created_at DESC " +
            "limit :limit offset :offset", nativeQuery = true)
    List<TransactionHistory> findTransactionsForRepairer(Long repairerId, Integer limit, Integer offset);

    long countByUserId(Long repairerId);

    @Query(value = "SELECT repairer.id as repairerId, th.id as transactionId, repairer.full_name as repairerName, repairer.phone as repairerPhone, " +
            "wr.type as withdrawType, th.transaction_code as transactionCode, th.amount " +
            "FROM transaction_histories th " +
            "JOIN users repairer " +
            "ON repairer.id = th.user_id " +
            "JOIN withdraw_requests wr " +
            "ON wr.id = th.withdraw_request_id " +
            "WHERE th.status = 'PENDING' " +
            "ORDER BY th.created_at DESC " +
            "limit :limit offset :offset", nativeQuery = true)
    List<IWithdrawHistoryDTO> findRepairerWithdrawHistoriesForAdmin(Integer limit, Integer offset);

    long countByTypeAndStatus(String type, String status);

    @Query(value = "SELECT th.id as transactionId, u.full_name as repairerName, u.phone as repairerPhone, wr.type as withdrawType, " +
            "th.transaction_code as transactionCode, th.amount, wr.bank_code as bankCode, wr.bank_account_number as bankAccountNumber, " +
            "wr.bank_account_name as bankAccountName " +
            "FROM transaction_histories th " +
            "JOIN users u " +
            "ON u.id = th.user_id " +
            "JOIN withdraw_requests wr " +
            "ON wr.id = th.withdraw_request_id " +
            "WHERE th.type = 'WITHDRAW' " +
            "AND th.id = :transactionId", nativeQuery = true)
    Optional<IWithdrawDetail> findRepairerWithdrawDetailForAdmin(Long transactionId);

    @Query(value = "SELECT u.id as repairerId, th.id as transactionId, u.full_name as repairerName, u.phone as repairerPhone, wr.type as withdrawType, " +
            "th.transaction_code as transactionCode, th.amount " +
            "FROM transaction_histories th " +
            "JOIN users u " +
            "ON u.id = th.user_id " +
            "JOIN withdraw_requests wr " +
            "ON wr.id = th.withdraw_request_id " +
            "WHERE th.type = 'WITHDRAW' " +
            "AND th.status = 'PENDING' " +
            "AND (CASE " +
            "       WHEN :withdrawType IS NOT NULL " +
            "       THEN wr.type = :withdrawType " +
            "       ELSE TRUE " +
            "END) " +
            "AND th.transaction_code " +
            "LIKE %:keyword%", nativeQuery = true)
    List<ISearchWithdrawDTO> searchRepairWithdrawHistoriesForAdmin(String keyword, String withdrawType);

    @Query(value = "SELECT count(*) FROM transaction_histories WHERE type = 'WITHDRAW' AND status = 'PENDING'", nativeQuery = true)
    long countPendingWithdraws();
}
