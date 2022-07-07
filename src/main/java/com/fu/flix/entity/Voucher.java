package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;

@Entity
@Data
@Table(name = "vouchers")
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private String description;

    private Long minOrderPrice;

    private Long remainQuantity;

    private LocalDateTime effectiveDate;

    private LocalDateTime expireDate;

    private boolean isDiscountMoney;

    @OneToMany(mappedBy = "voucher")
    private Collection<UserVoucher> userVouchers;

    @OneToMany(mappedBy = "voucher")
    private Collection<VoucherDevice> voucherDevices;

    @OneToMany(mappedBy = "voucher")
    private Collection<VoucherPaymentMethod> voucherPaymentMethods;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
