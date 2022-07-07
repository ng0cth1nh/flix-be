package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@Table(name = "invoice_accessoris")
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceAccessory {
    @EmbeddedId
    private InvoiceAccessoryId invoiceAccessoryId;

    @ManyToOne
    @MapsId("requestCode")
    @JoinColumn(name = "request_code")
    private Invoice invoice;

    @ManyToOne
    @MapsId("accessoryId")
    @JoinColumn(name = "accessory_id")
    private Accessory accessory;
}
