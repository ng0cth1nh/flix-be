package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Data
@Table(name = "invoices")
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    private String requestCode;

    private Long inspectionPrice;

    private Long totalSubServicePrice;

    private Long totalOtherPrice;

    private Long totalAccessoryPrice;

    private Long totalPrice;

    private Long voucherId;

    private Long totalDiscount;

    private Long actualProceeds;

    private LocalDateTime confirmedByRepairerAt;

    private Long vatPrice;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "invoice_sub_services",
            joinColumns = @JoinColumn(name = "request_code"),
            inverseJoinColumns = @JoinColumn(name = "sub_service_id"))
    private Collection<SubService> subServices = new ArrayList<>();

    @OneToMany(mappedBy = "invoice")
    private Collection<InvoiceAccessory> invoiceAccessories;

    @OneToMany(mappedBy = "invoice")
    private Collection<InvoiceOtherPrice> invoiceOtherPrices;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
