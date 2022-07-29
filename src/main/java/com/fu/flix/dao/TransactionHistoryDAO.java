package com.fu.flix.dao;

import com.fu.flix.dto.ITransactionDTO;
import com.fu.flix.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionHistoryDAO extends JpaRepository<TransactionHistory, Long> {
    Optional<TransactionHistory> findByRequestCodeAndType(String requestCode, String type);

    @Query(value = "SELECT th.id, th.request_code as requestCode, vt.transaction_no as vnpTransactionNo, th.amount, th.type as transactionType, u.full_name as fullName," +
            " u.phone, DATE_FORMAT(th.created_at, '%Y-%m-%d %H:%i:%s') as payDate " +
            "FROM transaction_histories th " +
            "LEFT JOIN vnpay_transactions vt " +
            "ON vt.vnp_txn_ref = th.request_code " +
            "JOIN users u " +
            "ON u.id = th.user_id limit :limit offset :offset", nativeQuery = true)
    List<ITransactionDTO> findTransactionsForAdmin(Integer limit, Integer offset);
}
