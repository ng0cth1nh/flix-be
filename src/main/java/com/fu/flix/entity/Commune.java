package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Data
@Table(name = "communes")
@NoArgsConstructor
@AllArgsConstructor
public class Commune {
    @Id
    private String id;

    private String name;

    private String type;

    private String districtId;

    @OneToMany(mappedBy = "commune", cascade = CascadeType.ALL)
    private Collection<UserAddress> userAddresses = new ArrayList<>();
}
