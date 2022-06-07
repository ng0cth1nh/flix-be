package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@Table(name = "user_addresses")
@NoArgsConstructor
@AllArgsConstructor
public class UserAddress {

    @EmbeddedId
    private UserAddressId id = new UserAddressId();

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("communeId")
    @JoinColumn(name = "commune_id")
    private Commune commune;

    private String streetAddress;

    private boolean isMainAddress;

    private String name;

    private String phone;

    private String addressCode;
}
