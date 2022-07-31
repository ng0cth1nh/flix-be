package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@Table(name = "bank_infos")
@NoArgsConstructor
@AllArgsConstructor
public class BankInfo {

    @Id
    private String id;

    private String engName;

    private String viName;
}
