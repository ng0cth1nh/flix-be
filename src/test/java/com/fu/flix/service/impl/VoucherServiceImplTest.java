package com.fu.flix.service.impl;

import com.fu.flix.dao.DiscountMoneyDAO;
import com.fu.flix.dao.DiscountPercentDAO;
import com.fu.flix.dao.VoucherDAO;
import com.fu.flix.dto.VoucherDTO;
import com.fu.flix.entity.DiscountMoney;
import com.fu.flix.entity.DiscountPercent;
import com.fu.flix.entity.Voucher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.transaction.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Transactional
class VoucherServiceImplTest {
    @Mock
    VoucherDAO voucherDAO;
    @Mock
    DiscountPercentDAO discountPercentDAO;
    @Mock
    DiscountMoneyDAO discountMoneyDAO;
    @InjectMocks
    VoucherServiceImpl underTest;

    @Mock
    Voucher voucher;

    @Mock
    DiscountMoney discountMoney;

    @Mock
    DiscountPercent discountPercent;

    @BeforeEach
    void setUp() {
        voucher = new Voucher();
        voucher.setMinOrderPrice(5000L);

        discountMoney = new DiscountMoney();
        discountMoney.setVoucherId(1L);
        discountMoney.setDiscountMoney(20000L);

        discountPercent = new DiscountPercent();
        discountPercent.setVoucherId(1L);
        discountPercent.setMaxDiscountPrice(30000L);
        discountPercent.setDiscountPercent(0.15);
    }

    @Test
    void test_getVoucherInfo_of_voucher_discount_percent() {
        // when
        voucher.setDiscountMoney(false);
        Mockito.when(voucherDAO.findById(1L)).thenReturn(Optional.of(voucher));
        Mockito.when(discountPercentDAO.findByVoucherId(1L)).thenReturn(Optional.of(discountPercent));
        VoucherDTO voucherInfo = underTest.getVoucherInfo(1L);

        // then
        Assertions.assertNotNull(voucherInfo);
    }

    @Test
    void test_getVoucherInfo_of_voucher_discount_money() {
        // when
        voucher.setDiscountMoney(true);
        Mockito.when(voucherDAO.findById(1L)).thenReturn(Optional.of(voucher));
        Mockito.when(discountMoneyDAO.findByVoucherId(1L)).thenReturn(Optional.of(discountMoney));
        VoucherDTO voucherInfo = underTest.getVoucherInfo(1L);

        // then
        Assertions.assertNotNull(voucherInfo);
    }

    @Test
    void test_getVoucherDiscount_when_voucher_not_found() {
        // when
        Mockito.when(voucherDAO.findById(1L)).thenReturn(Optional.empty());
        Long voucherDiscount = underTest.getVoucherDiscount(30000L, 1L);

        // then
        Assertions.assertEquals(0L, voucherDiscount);
    }

    @Test
    void test_getVoucherDiscount_when_voucher_is_discount_money() {
        // when
        voucher.setDiscountMoney(true);
        Mockito.when(voucherDAO.findById(1L)).thenReturn(Optional.of(voucher));
        Mockito.when(discountMoneyDAO.findByVoucherId(1L)).thenReturn(Optional.of(discountMoney));
        Long voucherDiscount = underTest.getVoucherDiscount(30000L, 1L);

        // then
        Assertions.assertEquals(20000L, voucherDiscount);
    }

    @Test
    void test_getVoucherDiscount_when_voucher_is_discount_percent() {
        // when
        voucher.setDiscountMoney(false);
        Mockito.when(voucherDAO.findById(1L)).thenReturn(Optional.of(voucher));
        discountPercent.setMaxDiscountPrice(15000L);
        Mockito.when(discountPercentDAO.findByVoucherId(1L)).thenReturn(Optional.of(discountPercent));
        Long voucherDiscount = underTest.getVoucherDiscount(30000L, 1L);

        // then
        Assertions.assertEquals(4500L, voucherDiscount);
    }

    @Test
    void test_getVoucherMinOrderPrice_when_voucher_is_null() {
        Long voucherMinOrderPrice = underTest.getVoucherMinOrderPrice(null);
        Assertions.assertEquals(0L, voucherMinOrderPrice);
    }

    @Test
    void test_getVoucherMinOrderPrice_when_voucher_is_not_found() {
        // when
        Mockito.when(voucherDAO.findById(1L)).thenReturn(Optional.empty());
        Long voucherMinOrderPrice = underTest.getVoucherMinOrderPrice(1L);
        Assertions.assertEquals(0L, voucherMinOrderPrice);
    }
}