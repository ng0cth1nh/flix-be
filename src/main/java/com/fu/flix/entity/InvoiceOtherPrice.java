package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@Table(name = "invoice_other_prices")
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceOtherPrice {
    @EmbeddedId
    private InvoiceOtherPriceId invoiceOtherPriceId;

    @ManyToOne
    @MapsId("requestCode")
    @JoinColumn(name = "request_code")
    private Invoice invoice;

    @ManyToOne
    @MapsId("otherPriceId")
    @JoinColumn(name = "other_price_id")
    private OtherPrice otherPrice;
}
