package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@Table(name = "voucher_payment_methods")
@NoArgsConstructor
@AllArgsConstructor
public class VoucherPaymentMethod {

    @EmbeddedId
    private VoucherPaymentMethodId voucherPaymentMethodId;

    @ManyToOne
    @MapsId("voucherId")
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @ManyToOne
    @MapsId("paymentMethodId")
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;
}
