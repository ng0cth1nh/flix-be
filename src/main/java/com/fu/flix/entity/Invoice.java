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

    private Long inspectionPrice;

    private Long totalServiceDetailPrice;

    private Long totalOtherPrice;

    private Long totalAccessoryPrice;

    private Long totalPrice;

    private Long voucherId;

    private Long totalDiscount;

    private Long actualProceeds;

    private LocalDateTime confirmedByRepairerAt;

    private Long vatPrice;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
