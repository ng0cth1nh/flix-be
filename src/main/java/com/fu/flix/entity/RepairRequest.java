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
@Table(name = "repair_requests")
@NoArgsConstructor
@AllArgsConstructor
public class RepairRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String requestCode;

    private Long userId;

    private Long serviceId;

    private String paymentMethodId;

    private String statusId;

    private LocalDateTime expectStartFixingAt;

    private String description;

    private Long voucherId;

    private Long addressId;

    private Double vat;

    private String cancelledByRoleId;

    private String reasonCancel;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
