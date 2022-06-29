package com.fu.flix.service;

import com.fu.flix.dto.VoucherDTO;

public interface VoucherService {
    VoucherDTO getVoucherInfo(Long voucherId);
    Double getVoucherDiscount(Double money, Long voucherId);
}
