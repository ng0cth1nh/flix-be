package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "discount_percents")
@NoArgsConstructor
@AllArgsConstructor
public class DiscountPercent {
    @Id
    private Long voucherId;

    private Double discountPercent;

    private Double maxDiscountPrice;
}
