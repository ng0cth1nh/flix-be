package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "status")
@NoArgsConstructor
@AllArgsConstructor
public class Status {
    @Id
    private String id;

    private String name;
}
