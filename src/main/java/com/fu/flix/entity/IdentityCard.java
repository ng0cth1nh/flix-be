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
@Table(name = "identity_cards")
@NoArgsConstructor
@AllArgsConstructor
public class IdentityCard {

    @Id
    private String identityCardNumber;

    private String type;

    private Long repairerId;

    private Long frontImageId;

    private Long backSideImageId;
}
