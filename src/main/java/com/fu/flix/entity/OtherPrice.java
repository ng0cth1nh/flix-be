package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Getter
@Setter
@Table(name = "other_prices")
@NoArgsConstructor
@AllArgsConstructor
public class OtherPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Long price;

    private String description;

    @OneToMany(mappedBy = "otherPrice")
    private Collection<InvoiceOtherPrice> invoiceOtherPrices;
}
