package com.fu.flix.service;

import com.fu.flix.entity.Invoice;

public interface FinanceService {
    Long getCommission(Invoice invoice);

    Long getProfit(Invoice invoice);
}
