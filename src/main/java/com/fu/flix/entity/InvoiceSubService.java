package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@Table(name = "invoice_sub_services")
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceSubService {
    @EmbeddedId
    private InvoiceSubServiceId invoiceSubServiceId;

    @ManyToOne
    @MapsId("requestCode")
    @JoinColumn(name = "request_code")
    private Invoice invoice;

    @ManyToOne
    @MapsId("subServiceId")
    @JoinColumn(name = "sub_service_id")
    private SubService subService;
}
