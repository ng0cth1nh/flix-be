package com.fu.flix.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class InvoiceAccessoryId implements Serializable {
    private String requestCode;

    private Long accessoryId;
}
