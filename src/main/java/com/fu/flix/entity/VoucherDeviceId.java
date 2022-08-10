package com.fu.flix.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class VoucherDeviceId implements Serializable {
    private String deviceId;

    private Long voucherId;
}
