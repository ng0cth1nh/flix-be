package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.entity.Invoice;
import com.fu.flix.service.FinanceService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class FinanceServiceImpl implements FinanceService {
    private final AppConf appConf;

    public FinanceServiceImpl(AppConf appConf) {
        this.appConf = appConf;
    }

    @Override
    public Long getCommission(Invoice invoice) {
        return getProfit(invoice) + invoice.getVatPrice();
    }

    @Override
    public Long getProfit(Invoice invoice) {
        return (long) ((invoice.getInspectionPrice() + invoice.getTotalSubServicePrice() + invoice.getTotalExtraServicePrice())
                * this.appConf.getProfitRate());
    }
}
