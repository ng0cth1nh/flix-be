package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Getter
@Setter
@Table(name = "repairers")
@NoArgsConstructor
@AllArgsConstructor
public class Repairer {
    @Id
    private Long userId;

    private String experienceDescription;

    private Integer experienceYear;

    private boolean isRepairing;

    private LocalDateTime acceptedAccountAt;

    private String cvStatus;

    private String commentCv;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "repairer_services",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"))
    private Collection<Service> services = new ArrayList<>();
}
