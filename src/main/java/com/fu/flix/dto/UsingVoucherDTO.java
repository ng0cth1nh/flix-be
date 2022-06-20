package com.fu.flix.dto;

import com.fu.flix.entity.UserVoucher;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;

@Data
@AllArgsConstructor
public class UsingVoucherDTO {
    private Collection<UserVoucher> userVouchers;
    private Long voucherId;
    private Long serviceId;
    private String paymentMethodId;
}
