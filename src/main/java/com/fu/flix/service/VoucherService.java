package com.fu.flix.service;

import com.fu.flix.dto.VoucherDTO;

public interface VoucherService {
    VoucherDTO getVoucherInfo(Long voucherId);
    Long getVoucherDiscount(Long money, Long voucherId);
    Long getVoucherMinOrderPrice(Long voucherId);
}
