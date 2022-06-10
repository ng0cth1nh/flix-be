package com.fu.flix.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class VoucherPaymentMethodId implements Serializable {
    private Long voucherId;

    private String paymentMethodId;
}
