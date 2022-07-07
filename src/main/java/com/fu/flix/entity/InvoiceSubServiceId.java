package com.fu.flix.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class InvoiceSubServiceId implements Serializable {
    private String requestCode;

    private Long subServiceId;
}
