package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "repair_requests_matching")
@NoArgsConstructor
@AllArgsConstructor
public class RepairRequestMatching {
    @Id
    private String requestCode;

    private Long repairerId;

    private Integer maxRepairInterval;

    private String statusId;

    private LocalDateTime startFixingAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
