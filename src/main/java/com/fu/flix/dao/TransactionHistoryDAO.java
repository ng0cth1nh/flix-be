package com.fu.flix.dao;

import com.fu.flix.dto.ITransactionDTO;
import com.fu.flix.dto.ITransactionDetailDTO;
import com.fu.flix.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionHistoryDAO extends JpaRepository<TransactionHistory, Long> {
    @Query(value = "SELECT th.id, th.transaction_code as transactionCode, vt.transaction_no as vnpTransactionNo, th.amount, th.type as transactionType, u.full_name as fullName, " +
            "u.phone, DATE_FORMAT(th.created_at, '%Y-%m-%d %H:%i:%s') as payDate " +
            "FROM transaction_histories th " +
            "LEFT JOIN vnpay_transactions vt " +
            "ON vt.id = th.vnpay_transaction_id " +
            "LEFT JOIN users u " +
            "ON u.id = th.user_id limit :limit offset :offset", nativeQuery = true)
    List<ITransactionDTO> findTransactionsForAdmin(Integer limit, Integer offset);

    @Query(value = "SELECT th.id, th.transaction_code as transactionCode, vt.transaction_no as vnpTransactionNo, th.amount, th.type as transactionType, u.full_name as fullName, " +
            "u.phone, DATE_FORMAT(th.created_at, '%Y-%m-%d %H:%i:%s') as payDate, vt.bank_code as bankCode, vt.card_type as cardType, " +
            "vt.order_info as orderInfo, vt.bank_tran_no as vnpBankTranNo,th.status as responseResult " +
            "FROM transaction_histories th " +
            "LEFT JOIN vnpay_transactions vt " +
            "ON vt.id = th.vnpay_transaction_id " +
            "LEFT JOIN users u " +
            "ON u.id = th.user_id " +
            "WHERE th.id = :id", nativeQuery = true)
    Optional<ITransactionDetailDTO> findTransactionDetail(Long id);

    @Query(value = "SELECT th.id, th.transaction_code as transactionCode, vt.transaction_no as vnpTransactionNo, th.amount, th.type as transactionType, u.full_name as fullName, " +
            "u.phone, DATE_FORMAT(th.created_at, '%Y-%m-%d %H:%i:%s') as payDate " +
            "FROM transaction_histories th " +
            "LEFT JOIN vnpay_transactions vt " +
            "ON vt.id = th.vnpay_transaction_id " +
            "LEFT JOIN users u " +
            "ON u.id = th.user_id " +
            "WHERE th.transaction_code LIKE %:keyword% " +
            "AND th.type IN (:transactionTypes)", nativeQuery = true)
    List<ITransactionDTO> searchTransactionsForAdmin(String keyword, List<String> transactionTypes);
}
