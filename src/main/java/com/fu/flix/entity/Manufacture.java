package com.fu.flix.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "manufactures")
@NoArgsConstructor
@AllArgsConstructor
public class Manufacture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}
