package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "invoices")
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    private String requestCode;

    private String paymentMethodId;

    private Double inspectionPrice;

    private Double totalServiceDetailPrice;

    private Double totalOtherPrice;

    private Double totalAccessoryPrice;

    private Double totalPrice;

    private Long voucherId;

    private Double totalDiscount;

    private Double actualProceeds;

    private LocalDateTime confirmedByRepairerAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
