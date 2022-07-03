package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "vnpay_transactions")
@NoArgsConstructor
@AllArgsConstructor
public class VnPayTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long amount;

    private String bankCode;

    private String bankTranNo;

    private String cardType;

    private String orderInfo;

    private String payDate;

    private String responseCode;

    private String tmnCode;

    private String transactionNo;

    private String transactionStatus;

    private String vnpTxnRef;

    private String secureHash;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
