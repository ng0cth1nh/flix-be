package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.enums.PaymentMethod;
import com.fu.flix.constant.enums.RequestStatus;
import com.fu.flix.dao.*;
import com.fu.flix.dto.request.CustomerPaymentUrlRequest;
import com.fu.flix.dto.response.CustomerPaymentUrlResponse;
import com.fu.flix.dto.security.UserPrincipal;
import com.fu.flix.entity.Invoice;
import com.fu.flix.entity.RepairRequest;
import com.fu.flix.service.FCMService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static com.fu.flix.constant.Constant.CREATE_PAYMENT_URL_SUCCESS;

@ExtendWith(MockitoExtension.class)
@Transactional
class VnPayServiceImplTest {
    VnPayServiceImpl underTest;
    @Mock
    AppConf appConf;
    @Mock
    RepairRequestDAO repairRequestDAO;
    @Mock
    VnPayTransactionDAO vnPayTransactionDAO;
    @Mock
    RepairRequestMatchingDAO repairRequestMatchingDAO;
    @Mock
    BalanceDAO balanceDAO;
    @Mock
    InvoiceDAO invoiceDAO;
    @Mock
    TransactionHistoryDAO transactionHistoryDAO;
    @Mock
    RepairerDAO repairerDAO;
    @Mock
    FCMService fcmService;
    @Mock
    Invoice invoice;
    @Mock
    RepairRequest repairRequest;

    @BeforeEach
    void setup() {
        AppConf.VnPayInfo vnPayInfo = new AppConf.VnPayInfo();
        vnPayInfo.setVnPayAmountRate(100);

        appConf = new AppConf();
        appConf.setVnPayInfo(vnPayInfo);
        appConf.getVnPayInfo().setVersion("2.1.0");
        appConf.getVnPayInfo().setCommand("pay");
        appConf.getVnPayInfo().setDatePattern("yyyyMMddHHmmss");

        underTest = new VnPayServiceImpl(appConf,
                repairRequestDAO,
                vnPayTransactionDAO,
                repairRequestMatchingDAO,
                balanceDAO,
                invoiceDAO,
                transactionHistoryDAO,
                repairerDAO,
                fcmService);

        invoice = new Invoice();
        invoice.setActualProceeds(25000L);

        repairRequest = new RepairRequest();
        repairRequest.setStatusId(RequestStatus.PAYMENT_WAITING.getId());
        repairRequest.setPaymentMethodId(PaymentMethod.VNPay.getId());
    }

    @Test
    void test_createCustomerPaymentUrl_success() {
        // given
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRemoteAddr("0:0:0:0:0:0:0:1");

        String requestCode = "ABC123456789";

        CustomerPaymentUrlRequest customerPaymentUrlRequest = new CustomerPaymentUrlRequest();
        customerPaymentUrlRequest.setOrderInfo("Meo Meo");
        customerPaymentUrlRequest.setRequestCode(requestCode);
        customerPaymentUrlRequest.setBankCode("NCB");

        setCustomerContext(36L, "0865390037");

        Mockito.when(invoiceDAO.findByRequestCode(requestCode)).thenReturn(Optional.of(invoice));
        Mockito.when(repairRequestDAO.findByUserIdAndRequestCode(36L, requestCode)).thenReturn(Optional.of(repairRequest));

        AppConf.PaymentInfo paymentInfo = new AppConf.PaymentInfo();
        paymentInfo.setReturnUrl("flix://request/invoice");
        paymentInfo.setSecureHash("MPBTQAIUBXZUPPPWJEGMCTPCDZRNRLZV");
        paymentInfo.setTmnCode("1TZDUKO5");

        appConf.getVnPayInfo().setPaymentInfo(paymentInfo);

        // when
        CustomerPaymentUrlResponse response = underTest.createCustomerPaymentUrl(customerPaymentUrlRequest, httpServletRequest).getBody();

        // then
        Assertions.assertEquals(CREATE_PAYMENT_URL_SUCCESS, response.getMessage());
    }

    void setCustomerContext(Long id, String phone) {
        String[] roles = {"ROLE_CUSTOMER"};
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(new UserPrincipal(id, phone, roles), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}