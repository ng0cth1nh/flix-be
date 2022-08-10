package com.fu.flix.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@Table(name = "voucher_devices")
@NoArgsConstructor
@AllArgsConstructor
public class VoucherDevice {
    @EmbeddedId
    private VoucherDeviceId voucherDeviceId;

    @ManyToOne
    @MapsId("voucherId")
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @ManyToOne
    @MapsId("deviceId")
    @JoinColumn(name = "device_id")
    private Device device;
}
