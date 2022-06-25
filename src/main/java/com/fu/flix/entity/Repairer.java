package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "repairers")
@NoArgsConstructor
@AllArgsConstructor
public class Repairer {
    @Id
    private Long userId;

    private String username;

    private String experience;

    private String certificate;

    private boolean isRepairing;

    private LocalDateTime acceptedAccountAt;
}
