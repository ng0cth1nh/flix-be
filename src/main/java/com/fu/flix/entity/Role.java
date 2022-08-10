package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    private String id;

    private String name;
}
