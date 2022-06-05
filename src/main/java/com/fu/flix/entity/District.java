package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "districts")
@NoArgsConstructor
@AllArgsConstructor
public class District {
    @Id
    private String id;

    private String name;

    private String type;

    private String city_id;
}
