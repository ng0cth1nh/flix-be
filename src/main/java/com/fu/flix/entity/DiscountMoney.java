package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "discount_moneys")
@NoArgsConstructor
@AllArgsConstructor
public class DiscountMoney {

    @Id
    private Long voucherId;

    private Long discountMoney;
}
