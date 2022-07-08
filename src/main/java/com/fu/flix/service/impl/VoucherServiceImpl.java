package com.fu.flix.service.impl;

import com.fu.flix.dao.DiscountMoneyDAO;
import com.fu.flix.dao.DiscountPercentDAO;
import com.fu.flix.dao.VoucherDAO;
import com.fu.flix.dto.VoucherDTO;
import com.fu.flix.entity.DiscountMoney;
import com.fu.flix.entity.DiscountPercent;
import com.fu.flix.entity.Voucher;
import com.fu.flix.service.VoucherService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VoucherServiceImpl implements VoucherService {
    private final VoucherDAO voucherDAO;
    private final DiscountPercentDAO discountPercentDAO;
    private final DiscountMoneyDAO discountMoneyDAO;
    private final String PERCENT = "%";
    private final Integer ONE_HUNDRED = 100;
    private final String VND_UNIT = "Đồng";
    private final String SPACE = " ";

    public VoucherServiceImpl(VoucherDAO voucherDAO,
                              DiscountPercentDAO discountPercentDAO,
                              DiscountMoneyDAO discountMoneyDAO) {
        this.voucherDAO = voucherDAO;
        this.discountPercentDAO = discountPercentDAO;
        this.discountMoneyDAO = discountMoneyDAO;
    }

    @Override
    public VoucherDTO getVoucherInfo(Long voucherId) {
        VoucherDTO voucherDTO = new VoucherDTO();
        if (voucherId != null) {
            Optional<Voucher> optionalVoucher = voucherDAO.findById(voucherId);
            if (optionalVoucher.isPresent()) {
                Voucher voucher = optionalVoucher.get();
                voucherDTO.setVoucherDescription(voucher.getDescription());

                if (voucher.isDiscountMoney()) {
                    DiscountMoney discountMoney = discountMoneyDAO.findByVoucherId(voucherId).get();
                    voucherDTO.setVoucherDiscount(discountMoney.getDiscountMoney() + SPACE + VND_UNIT);
                } else {
                    DiscountPercent discountPercent = discountPercentDAO.findByVoucherId(voucherId).get();
                    voucherDTO.setVoucherDiscount(discountPercent.getDiscountPercent() * ONE_HUNDRED + PERCENT);
                }
            }
        }
        return voucherDTO;
    }

    @Override
    public Long getVoucherDiscount(Long money, Long voucherId) {
        long discount = 0;
        if (voucherId == null) {
            return discount;
        }

        Optional<Voucher> optionalVoucher = voucherDAO.findById(voucherId);
        if (optionalVoucher.isEmpty()) {
            return discount;
        }

        Voucher voucher = optionalVoucher.get();
        if (money < voucher.getMinOrderPrice()) {
            return discount;
        }

        if (voucher.isDiscountMoney()) {
            DiscountMoney discountMoney = discountMoneyDAO.findByVoucherId(voucherId).get();
            return discountMoney.getDiscountMoney();
        }

        DiscountPercent discountPercent = discountPercentDAO.findByVoucherId(voucherId).get();
        discount = (long) (discountPercent.getDiscountPercent() * money);
        return discount > discountPercent.getMaxDiscountPrice()
                ? discountPercent.getMaxDiscountPrice()
                : discount;
    }

    @Override
    public Long getVoucherMinOrderPrice(Long voucherId) {
        long min = 0;
        if (voucherId == null) {
            return min;
        }

        Optional<Voucher> optionalVoucher = voucherDAO.findById(voucherId);
        if (optionalVoucher.isEmpty()) {
            return min;
        }

        return optionalVoucher.get().getMinOrderPrice();
    }
}
