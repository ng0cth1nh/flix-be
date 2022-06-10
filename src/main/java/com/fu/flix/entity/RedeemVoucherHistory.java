package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "redeem_voucher_histories")
@NoArgsConstructor
@AllArgsConstructor
public class RedeemVoucherHistory {
    @Id
    private Long userId;

    private Long usedPoint;

    private Long quantity;

    private Long receivedVoucherId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
