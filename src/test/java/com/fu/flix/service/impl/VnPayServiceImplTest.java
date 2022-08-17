package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.enums.PaymentMethod;
import com.fu.flix.constant.enums.RequestStatus;
import com.fu.flix.dao.*;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.CustomerPaymentUrlRequest;
import com.fu.flix.dto.request.RepairerDepositUrlRequest;
import com.fu.flix.dto.response.CustomerPaymentResponse;
import com.fu.flix.dto.response.CustomerPaymentUrlResponse;
import com.fu.flix.dto.response.RepairerDepositResponse;
import com.fu.flix.dto.response.RepairerDepositUrlResponse;
import com.fu.flix.dto.security.UserPrincipal;
import com.fu.flix.entity.*;
import com.fu.flix.service.FCMService;
import org.apache.logging.log4j.util.Strings;
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
import java.util.*;

import static com.fu.flix.constant.Constant.*;

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
    @Mock
    RepairRequestMatching repairRequestMatching;
    @Mock
    Repairer repairer;
    @Mock
    Balance balance;

    @Mock
    VnPayTransaction vnPayTransaction;
    private final String VNP_TRANSACTION_NO = "vnp_TransactionNo";
    private final String VNP_TRANSACTION_STATUS = "vnp_TransactionStatus";
    private final String VN_PAY_SUCCESS_CODE = "00";
    private final String VNP_AMOUNT = "vnp_Amount";
    private final String VNP_TNX_REF = "vnp_TxnRef";
    private final String VNP_SECURE_HASH = "vnp_SecureHash";
    private final String VNP_RESPONSE_CODE = "vnp_ResponseCode";
    private final String VNP_BANK_CODE = "vnp_BankCode";
    private final String VNP_BANK_TRAN_NO = "vnp_BankTranNo";
    private final String VNP_CARD_TYPE = "vnp_CardType";
    private final String VNP_ORDER_INFO = "vnp_OrderInfo";
    private final String VNP_PAY_DATE = "vnp_PayDate";
    private final String VNP_TMN_CODE = "vnp_TmnCode";

    @BeforeEach
    void setup() {
        AppConf.VnPayInfo vnPayInfo = new AppConf.VnPayInfo();
        vnPayInfo.setVnPayAmountRate(100);

        AppConf.DepositInfo depositInfo = new AppConf.DepositInfo();
        depositInfo.setReturnUrl("flix://request/invoice");
        depositInfo.setSecureHash("MPBTQAIUBXZUPPPWJEGMCTPCDZRNRLZV");
        depositInfo.setTmnCode("1TZDUKO5");

        AppConf.PaymentInfo paymentInfo = new AppConf.PaymentInfo();
        paymentInfo.setReturnUrl("flix://request/invoice");
        paymentInfo.setSecureHash("MPBTQAIUBXZUPPPWJEGMCTPCDZRNRLZV");
        paymentInfo.setTmnCode("1TZDUKO5");

        appConf = new AppConf();
        appConf.setVnPayInfo(vnPayInfo);
        appConf.getVnPayInfo().setVersion("2.1.0");
        appConf.getVnPayInfo().setCommand("pay");
        appConf.getVnPayInfo().setDatePattern("yyyyMMddHHmmss");
        appConf.setMinVnPay(5000L);
        appConf.getVnPayInfo().setDepositInfo(depositInfo);
        appConf.getVnPayInfo().setPaymentInfo(paymentInfo);

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
        invoice.setActualProceeds(31500L);

        repairRequest = new RepairRequest();
        repairRequest.setStatusId(RequestStatus.PAYMENT_WAITING.getId());
        repairRequest.setPaymentMethodId(PaymentMethod.VNPay.getId());

        repairRequestMatching = new RepairRequestMatching();
        repairRequestMatching.setRepairerId(52L);

        balance = new Balance();
        balance.setBalance(999999L);

        vnPayTransaction = new VnPayTransaction();
        vnPayTransaction.setId(1L);
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

        // when
        CustomerPaymentUrlResponse response = underTest.createCustomerPaymentUrl(customerPaymentUrlRequest, httpServletRequest).getBody();

        // then
        Assertions.assertEquals(CREATE_PAYMENT_URL_SUCCESS, response.getMessage());
    }

    @Test
    void test_createCustomerPaymentUrl_fail_when_order_info_is_null() {
        // given
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRemoteAddr("0:0:0:0:0:0:0:1");

        String requestCode = "ABC123456789";

        CustomerPaymentUrlRequest customerPaymentUrlRequest = new CustomerPaymentUrlRequest();
        customerPaymentUrlRequest.setOrderInfo(null);
        customerPaymentUrlRequest.setRequestCode(requestCode);
        customerPaymentUrlRequest.setBankCode("NCB");

        setCustomerContext(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class,
                () -> underTest.createCustomerPaymentUrl(customerPaymentUrlRequest, httpServletRequest));

        // then
        Assertions.assertEquals(ORDER_INFO_IS_REQUIRED, exception.getMessage());
    }

    @Test
    void test_createCustomerPaymentUrl_fail_when_bank_code_is_null() {
        // given
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRemoteAddr("0:0:0:0:0:0:0:1");

        String requestCode = "ABC123456789";

        CustomerPaymentUrlRequest customerPaymentUrlRequest = new CustomerPaymentUrlRequest();
        customerPaymentUrlRequest.setOrderInfo("meo meo");
        customerPaymentUrlRequest.setRequestCode(requestCode);
        customerPaymentUrlRequest.setBankCode(null);

        setCustomerContext(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class,
                () -> underTest.createCustomerPaymentUrl(customerPaymentUrlRequest, httpServletRequest));

        // then
        Assertions.assertEquals(BANK_CODE_IS_REQUIRED, exception.getMessage());
    }

    @Test
    void test_createCustomerPaymentUrl_fail_when_request_code_is_null() {
        // given
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRemoteAddr("0:0:0:0:0:0:0:1");

        CustomerPaymentUrlRequest customerPaymentUrlRequest = new CustomerPaymentUrlRequest();
        customerPaymentUrlRequest.setOrderInfo("meo meo");
        customerPaymentUrlRequest.setRequestCode(null);
        customerPaymentUrlRequest.setBankCode("NCB");

        setCustomerContext(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class,
                () -> underTest.createCustomerPaymentUrl(customerPaymentUrlRequest, httpServletRequest));

        // then
        Assertions.assertEquals(REQUEST_CODE_IS_REQUIRED, exception.getMessage());
    }

    @Test
    void test_createCustomerPaymentUrl_fail_when_invalid_request_code() {
        // given
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRemoteAddr("0:0:0:0:0:0:0:1");

        String requestCode = "ABC123456789";

        CustomerPaymentUrlRequest customerPaymentUrlRequest = new CustomerPaymentUrlRequest();
        customerPaymentUrlRequest.setOrderInfo("meo meo");
        customerPaymentUrlRequest.setRequestCode(requestCode);
        customerPaymentUrlRequest.setBankCode("NCB");

        Mockito.when(repairRequestDAO.findByUserIdAndRequestCode(36L, requestCode)).thenReturn(Optional.empty());

        setCustomerContext(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class,
                () -> underTest.createCustomerPaymentUrl(customerPaymentUrlRequest, httpServletRequest));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    void test_createCustomerPaymentUrl_fail_when_request_status_is_not_PW() {
        // given
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRemoteAddr("0:0:0:0:0:0:0:1");

        String requestCode = "ABC123456789";

        CustomerPaymentUrlRequest customerPaymentUrlRequest = new CustomerPaymentUrlRequest();
        customerPaymentUrlRequest.setOrderInfo("meo meo");
        customerPaymentUrlRequest.setRequestCode(requestCode);
        customerPaymentUrlRequest.setBankCode("NCB");

        repairRequest.setStatusId(RequestStatus.PENDING.getId());
        Mockito.when(repairRequestDAO.findByUserIdAndRequestCode(36L, requestCode)).thenReturn(Optional.of(repairRequest));

        setCustomerContext(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class,
                () -> underTest.createCustomerPaymentUrl(customerPaymentUrlRequest, httpServletRequest));

        // then
        Assertions.assertEquals(CUSTOMER_PAYMENT_ONLY_USE_WHEN_STATUS_IS_PAYMENT_WAITING, exception.getMessage());
    }

    @Test
    void test_createCustomerPaymentUrl_fail_when_payment_method_is_not_VNPay() {
        // given
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRemoteAddr("0:0:0:0:0:0:0:1");

        String requestCode = "ABC123456789";

        CustomerPaymentUrlRequest customerPaymentUrlRequest = new CustomerPaymentUrlRequest();
        customerPaymentUrlRequest.setOrderInfo("meo meo");
        customerPaymentUrlRequest.setRequestCode(requestCode);
        customerPaymentUrlRequest.setBankCode("NCB");

        repairRequest.setPaymentMethodId(PaymentMethod.CASH.getId());
        Mockito.when(repairRequestDAO.findByUserIdAndRequestCode(36L, requestCode)).thenReturn(Optional.of(repairRequest));

        setCustomerContext(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class,
                () -> underTest.createCustomerPaymentUrl(customerPaymentUrlRequest, httpServletRequest));

        // then
        Assertions.assertEquals(PAYMENT_METHOD_MUST_BE_VN_PAY, exception.getMessage());
    }

    @Test
    void test_responseCustomerPayment_success() {
        // given
        String requestCode = "170822PK5LBW";
        long repairerId = 52L;

        Map<String, String> requestParams = new HashMap() {{
            put(VNP_AMOUNT, "3150000");
            put(VNP_BANK_CODE, "NCB");
            put(VNP_BANK_TRAN_NO, "VNP13818300");
            put(VNP_CARD_TYPE, "ATM");
            put(VNP_ORDER_INFO, "sieu+pham+2022");
            put(VNP_PAY_DATE, "20220817092228");
            put(VNP_RESPONSE_CODE, "00");
            put(VNP_TMN_CODE, "1TZDUKO5");
            put(VNP_TRANSACTION_NO, "13818300");
            put(VNP_TRANSACTION_STATUS, "00");
            put(VNP_TNX_REF, requestCode);
            put(VNP_SECURE_HASH, "969077a7ba3a361cb14f96f1eb956c128c90a1c1ac6ba4c0dc28f16bf3e302a632b5897f5847e8c22729b1c2af8f1200616b96c9c2f01cc49a079e7b33835c0c");
        }};

        Mockito.when(repairRequestDAO.findByRequestCode(requestCode)).thenReturn(Optional.of(repairRequest));
        Mockito.when(repairRequestMatchingDAO.findByRequestCode(requestCode)).thenReturn(Optional.of(repairRequestMatching));
        Mockito.when(repairerDAO.findByUserId(repairerId)).thenReturn(Optional.of(repairer));
        Mockito.when(invoiceDAO.findByRequestCode(requestCode)).thenReturn(Optional.of(invoice));
        Mockito.when(balanceDAO.findByUserId(repairerId)).thenReturn(Optional.of(balance));
        Mockito.when(vnPayTransactionDAO.save(Mockito.any(VnPayTransaction.class))).thenReturn(vnPayTransaction);

        // when
        CustomerPaymentResponse response = underTest.responseCustomerPayment(requestParams).getBody();

        // then
        Assertions.assertEquals(PAYMENT_SUCCESS, response.getMessage());
    }

    @Test
    void test_responseCustomerPayment_fail_when_secure_hash_is_null() {
        // given
        String requestCode = "170822PK5LBW";

        Map<String, String> requestParams = new HashMap() {{
            put(VNP_AMOUNT, "3150000");
            put(VNP_BANK_CODE, "NCB");
            put(VNP_BANK_TRAN_NO, "VNP13818300");
            put(VNP_CARD_TYPE, "ATM");
            put(VNP_ORDER_INFO, "sieu+pham+2022");
            put(VNP_PAY_DATE, "20220817092228");
            put(VNP_RESPONSE_CODE, "00");
            put(VNP_TMN_CODE, "1TZDUKO5");
            put(VNP_TRANSACTION_NO, "13818300");
            put(VNP_TRANSACTION_STATUS, "00");
            put(VNP_TNX_REF, requestCode);
        }};

        Mockito.when(vnPayTransactionDAO.save(Mockito.any(VnPayTransaction.class))).thenReturn(vnPayTransaction);

        // when
        CustomerPaymentResponse response = underTest.responseCustomerPayment(requestParams).getBody();

        // then
        Assertions.assertEquals(INVALID_CHECKSUM, response.getMessage());
    }

    @Test
    void test_responseCustomerPayment_fail_when_tnx_ref_is_null() {
        // given
        Map<String, String> requestParams = new HashMap() {{
            put(VNP_AMOUNT, "3150000");
            put(VNP_BANK_CODE, "NCB");
            put(VNP_BANK_TRAN_NO, "VNP13818300");
            put(VNP_CARD_TYPE, "ATM");
            put(VNP_ORDER_INFO, "sieu+pham+2022");
            put(VNP_PAY_DATE, "20220817092228");
            put(VNP_RESPONSE_CODE, "00");
            put(VNP_TMN_CODE, "1TZDUKO5");
            put(VNP_TRANSACTION_NO, "13818300");
            put(VNP_TRANSACTION_STATUS, "00");
            put(VNP_SECURE_HASH, "a4cc851c98e023d1b0f1d473ded7804753cf2bdec3cd1aeae5e4285254dc91b6d79e2ca5b274d26f07a79ecffd2d9239dd8e32df6d929f0831472ff410c6ac33");
        }};

        Mockito.when(vnPayTransactionDAO.save(Mockito.any(VnPayTransaction.class))).thenReturn(vnPayTransaction);

        // when
        CustomerPaymentResponse response = underTest.responseCustomerPayment(requestParams).getBody();

        // then
        Assertions.assertEquals(VNP_TXN_REF_IS_REQUIRED, response.getMessage());
    }

    @Test
    void test_responseCustomerPayment_fail_when_repair_request_not_found() {
        // given
        String requestCode = "170822PK5LBW";
        Map<String, String> requestParams = new HashMap() {{
            put(VNP_AMOUNT, "3150000");
            put(VNP_BANK_CODE, "NCB");
            put(VNP_BANK_TRAN_NO, "VNP13818300");
            put(VNP_CARD_TYPE, "ATM");
            put(VNP_ORDER_INFO, "sieu+pham+2022");
            put(VNP_PAY_DATE, "20220817092228");
            put(VNP_RESPONSE_CODE, "00");
            put(VNP_TMN_CODE, "1TZDUKO5");
            put(VNP_TRANSACTION_NO, "13818300");
            put(VNP_TRANSACTION_STATUS, "00");
            put(VNP_TNX_REF, requestCode);
            put(VNP_SECURE_HASH, "969077a7ba3a361cb14f96f1eb956c128c90a1c1ac6ba4c0dc28f16bf3e302a632b5897f5847e8c22729b1c2af8f1200616b96c9c2f01cc49a079e7b33835c0c");
        }};

        Mockito.when(repairRequestDAO.findByRequestCode(requestCode)).thenReturn(Optional.empty());
        Mockito.when(vnPayTransactionDAO.save(Mockito.any(VnPayTransaction.class))).thenReturn(vnPayTransaction);

        // when
        CustomerPaymentResponse response = underTest.responseCustomerPayment(requestParams).getBody();

        // then
        Assertions.assertEquals(REPAIR_REQUEST_NOT_FOUND, response.getMessage());
    }

    @Test
    void test_responseCustomerPayment_fail_when_request_status_is_not_PW() {
        // given
        String requestCode = "170822PK5LBW";
        Map<String, String> requestParams = new HashMap() {{
            put(VNP_AMOUNT, "3150000");
            put(VNP_BANK_CODE, "NCB");
            put(VNP_BANK_TRAN_NO, "VNP13818300");
            put(VNP_CARD_TYPE, "ATM");
            put(VNP_ORDER_INFO, "sieu+pham+2022");
            put(VNP_PAY_DATE, "20220817092228");
            put(VNP_RESPONSE_CODE, "00");
            put(VNP_TMN_CODE, "1TZDUKO5");
            put(VNP_TRANSACTION_NO, "13818300");
            put(VNP_TRANSACTION_STATUS, "00");
            put(VNP_TNX_REF, requestCode);
            put(VNP_SECURE_HASH, "969077a7ba3a361cb14f96f1eb956c128c90a1c1ac6ba4c0dc28f16bf3e302a632b5897f5847e8c22729b1c2af8f1200616b96c9c2f01cc49a079e7b33835c0c");
        }};

        repairRequest.setStatusId(RequestStatus.DONE.getId());

        Mockito.when(repairRequestDAO.findByRequestCode(requestCode)).thenReturn(Optional.of(repairRequest));
        Mockito.when(vnPayTransactionDAO.save(Mockito.any(VnPayTransaction.class))).thenReturn(vnPayTransaction);
        Mockito.when(repairRequestMatchingDAO.findByRequestCode(requestCode)).thenReturn(Optional.of(repairRequestMatching));
        Mockito.when(repairerDAO.findByUserId(52L)).thenReturn(Optional.of(repairer));

        // when
        CustomerPaymentResponse response = underTest.responseCustomerPayment(requestParams).getBody();

        // then
        Assertions.assertEquals(CUSTOMER_PAYMENT_ONLY_USE_WHEN_STATUS_IS_PAYMENT_WAITING, response.getMessage());
    }

    @Test
    void test_responseCustomerPayment_fail_when_request_does_not_match_with_any_repairer() {
        // given
        String requestCode = "170822PK5LBW";
        Map<String, String> requestParams = new HashMap() {{
            put(VNP_AMOUNT, "3150000");
            put(VNP_BANK_CODE, "NCB");
            put(VNP_BANK_TRAN_NO, "VNP13818300");
            put(VNP_CARD_TYPE, "ATM");
            put(VNP_ORDER_INFO, "sieu+pham+2022");
            put(VNP_PAY_DATE, "20220817092228");
            put(VNP_RESPONSE_CODE, "00");
            put(VNP_TMN_CODE, "1TZDUKO5");
            put(VNP_TRANSACTION_NO, "13818300");
            put(VNP_TRANSACTION_STATUS, "00");
            put(VNP_TNX_REF, requestCode);
            put(VNP_SECURE_HASH, "969077a7ba3a361cb14f96f1eb956c128c90a1c1ac6ba4c0dc28f16bf3e302a632b5897f5847e8c22729b1c2af8f1200616b96c9c2f01cc49a079e7b33835c0c");
        }};

        repairRequest.setStatusId(RequestStatus.PENDING.getId());

        Mockito.when(repairRequestDAO.findByRequestCode(requestCode)).thenReturn(Optional.of(repairRequest));
        Mockito.when(vnPayTransactionDAO.save(Mockito.any(VnPayTransaction.class))).thenReturn(vnPayTransaction);

        // when
        CustomerPaymentResponse response = underTest.responseCustomerPayment(requestParams).getBody();

        // then
        Assertions.assertEquals(REQUEST_DOES_NOT_MATCHING_WITH_ANY_REPAIRER, response.getMessage());
    }

    @Test
    void test_responseCustomerPayment_fail_when_response_code_is_not_00() {
        // given
        String requestCode = "170822PK5LBW";
        Map<String, String> requestParams = new HashMap() {{
            put(VNP_AMOUNT, "3150000");
            put(VNP_BANK_CODE, "NCB");
            put(VNP_BANK_TRAN_NO, "VNP13818300");
            put(VNP_CARD_TYPE, "ATM");
            put(VNP_ORDER_INFO, "sieu+pham+2022");
            put(VNP_PAY_DATE, "20220817092228");
            put(VNP_RESPONSE_CODE, "01");
            put(VNP_TMN_CODE, "1TZDUKO5");
            put(VNP_TRANSACTION_NO, "13818300");
            put(VNP_TRANSACTION_STATUS, "00");
            put(VNP_TNX_REF, requestCode);
            put(VNP_SECURE_HASH, "6e1aac34e97369b537011008ac0fc7ea6c4bcebf409178982de7f2cc4db63f74ae2adc55990a567b6d0715e5b065bb282774d68582ee8198d34f80228c3d6715");
        }};

        Mockito.when(repairRequestDAO.findByRequestCode(requestCode)).thenReturn(Optional.of(repairRequest));
        Mockito.when(vnPayTransactionDAO.save(Mockito.any(VnPayTransaction.class))).thenReturn(vnPayTransaction);
        Mockito.when(repairRequestMatchingDAO.findByRequestCode(requestCode)).thenReturn(Optional.of(repairRequestMatching));
        Mockito.when(repairerDAO.findByUserId(52L)).thenReturn(Optional.of(repairer));

        // when
        CustomerPaymentResponse response = underTest.responseCustomerPayment(requestParams).getBody();

        // then
        Assertions.assertEquals(PAYMENT_FAILED, response.getMessage());
    }

    @Test
    void test_responseCustomerPayment_fail_when_txn_ref_is_existed() {
        // given
        String requestCode = "170822PK5LBW";
        Map<String, String> requestParams = new HashMap() {{
            put(VNP_AMOUNT, "3150000");
            put(VNP_BANK_CODE, "NCB");
            put(VNP_BANK_TRAN_NO, "VNP13818300");
            put(VNP_CARD_TYPE, "ATM");
            put(VNP_ORDER_INFO, "sieu+pham+2022");
            put(VNP_PAY_DATE, "20220817092228");
            put(VNP_RESPONSE_CODE, "00");
            put(VNP_TMN_CODE, "1TZDUKO5");
            put(VNP_TRANSACTION_NO, "13818300");
            put(VNP_TRANSACTION_STATUS, "00");
            put(VNP_TNX_REF, requestCode);
            put(VNP_SECURE_HASH, "969077a7ba3a361cb14f96f1eb956c128c90a1c1ac6ba4c0dc28f16bf3e302a632b5897f5847e8c22729b1c2af8f1200616b96c9c2f01cc49a079e7b33835c0c");
        }};

        Mockito.when(repairRequestDAO.findByRequestCode(requestCode)).thenReturn(Optional.of(repairRequest));
        Mockito.when(vnPayTransactionDAO.save(Mockito.any(VnPayTransaction.class))).thenReturn(vnPayTransaction);
        Mockito.when(repairRequestMatchingDAO.findByRequestCode(requestCode)).thenReturn(Optional.of(repairRequestMatching));
        Mockito.when(repairerDAO.findByUserId(52L)).thenReturn(Optional.of(repairer));
        Mockito.when(vnPayTransactionDAO.findByVnpTxnRefAndResponseCode(requestCode, VN_PAY_SUCCESS_CODE)).thenReturn(Optional.of(vnPayTransaction));

        // when
        CustomerPaymentResponse response = underTest.responseCustomerPayment(requestParams).getBody();

        // then
        Assertions.assertEquals(VNP_TXN_REF_EXISTED_IN_DATABASE, response.getMessage());
    }

    @Test
    void test_responseCustomerPayment_fail_when_amount_does_not_match() {
        // given
        String requestCode = "170822PK5LBW";
        Map<String, String> requestParams = new HashMap() {{
            put(VNP_AMOUNT, "3250000");
            put(VNP_BANK_CODE, "NCB");
            put(VNP_BANK_TRAN_NO, "VNP13818300");
            put(VNP_CARD_TYPE, "ATM");
            put(VNP_ORDER_INFO, "sieu+pham+2022");
            put(VNP_PAY_DATE, "20220817092228");
            put(VNP_RESPONSE_CODE, "00");
            put(VNP_TMN_CODE, "1TZDUKO5");
            put(VNP_TRANSACTION_NO, "13818300");
            put(VNP_TRANSACTION_STATUS, "00");
            put(VNP_TNX_REF, requestCode);
            put(VNP_SECURE_HASH, "aa87c6d90e434ff4b48fa2edc6f5cd5ffc0b30ec1702bd866b175aee57134ccf1f30dbde87105ef74b723b85d59ddfacdca6b6b7c8b13ce1eb364dc48621c03b");
        }};

        Mockito.when(repairRequestDAO.findByRequestCode(requestCode)).thenReturn(Optional.of(repairRequest));
        Mockito.when(vnPayTransactionDAO.save(Mockito.any(VnPayTransaction.class))).thenReturn(vnPayTransaction);
        Mockito.when(repairRequestMatchingDAO.findByRequestCode(requestCode)).thenReturn(Optional.of(repairRequestMatching));
        Mockito.when(repairerDAO.findByUserId(52L)).thenReturn(Optional.of(repairer));
        Mockito.when(vnPayTransactionDAO.findByVnpTxnRefAndResponseCode(requestCode, VN_PAY_SUCCESS_CODE)).thenReturn(Optional.empty());
        Mockito.when(invoiceDAO.findByRequestCode(requestCode)).thenReturn(Optional.of(invoice));

        // when
        CustomerPaymentResponse response = underTest.responseCustomerPayment(requestParams).getBody();

        // then
        Assertions.assertEquals(AMOUNT_DOES_NOT_MATCH_TO_INVOICE, response.getMessage());
    }

    @Test
    void test_createRepairerDepositUrl_success() {
        // given
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRemoteAddr("0:0:0:0:0:0:0:1");

        RepairerDepositUrlRequest repairerDepositUrlRequest = new RepairerDepositUrlRequest();
        repairerDepositUrlRequest.setOrderInfo("Meo Meo");
        repairerDepositUrlRequest.setAmount(31500L);
        repairerDepositUrlRequest.setBankCode("NCB");

        setRepairerContext(52L, "0865390037");

        // when
        RepairerDepositUrlResponse response = underTest.createRepairerDepositUrl(repairerDepositUrlRequest, httpServletRequest).getBody();

        // then
        Assertions.assertEquals(CREATE_PAYMENT_URL_SUCCESS, response.getMessage());
    }

    @Test
    void test_createRepairerDepositUrl_fail_when_order_info_is_null() {
        // given
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRemoteAddr("0:0:0:0:0:0:0:1");

        RepairerDepositUrlRequest repairerDepositUrlRequest = new RepairerDepositUrlRequest();
        repairerDepositUrlRequest.setOrderInfo(null);
        repairerDepositUrlRequest.setAmount(31500L);
        repairerDepositUrlRequest.setBankCode("NCB");

        setRepairerContext(52L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createRepairerDepositUrl(repairerDepositUrlRequest, httpServletRequest));

        // then
        Assertions.assertEquals(ORDER_INFO_IS_REQUIRED, exception.getMessage());
    }

    @Test
    void test_createRepairerDepositUrl_fail_when_bank_code_is_null() {
        // given
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRemoteAddr("0:0:0:0:0:0:0:1");

        RepairerDepositUrlRequest repairerDepositUrlRequest = new RepairerDepositUrlRequest();
        repairerDepositUrlRequest.setOrderInfo("meo meo");
        repairerDepositUrlRequest.setAmount(31500L);
        repairerDepositUrlRequest.setBankCode(null);

        setRepairerContext(52L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class,
                () -> underTest.createRepairerDepositUrl(repairerDepositUrlRequest, httpServletRequest));

        // then
        Assertions.assertEquals(BANK_CODE_IS_REQUIRED, exception.getMessage());
    }

    @Test
    void test_createRepairerDepositUrl_fail_when_amount_less_than_5000() {
        // given
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRemoteAddr("0:0:0:0:0:0:0:1");

        RepairerDepositUrlRequest repairerDepositUrlRequest = new RepairerDepositUrlRequest();
        repairerDepositUrlRequest.setOrderInfo("meo meo");
        repairerDepositUrlRequest.setAmount(2000L);
        repairerDepositUrlRequest.setBankCode("NCB");

        setRepairerContext(52L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class,
                () -> underTest.createRepairerDepositUrl(repairerDepositUrlRequest, httpServletRequest));

        // then
        Assertions.assertEquals(AMOUNT_MUST_BE_GREATER_OR_EQUAL_ + "5000", exception.getMessage());
    }

    @Test
    void test_responseRepairerDeposit_success() {
        // given
        Map<String, String> requestParams = new HashMap() {{
            put(VNP_AMOUNT, "3150000");
            put(VNP_BANK_CODE, "NCB");
            put(VNP_BANK_TRAN_NO, "VNP13818300");
            put(VNP_CARD_TYPE, "ATM");
            put(VNP_ORDER_INFO, "sieu+pham+2022");
            put(VNP_PAY_DATE, "20220817092228");
            put(VNP_RESPONSE_CODE, "00");
            put(VNP_TMN_CODE, "1TZDUKO5");
            put(VNP_TRANSACTION_NO, "13818300");
            put(VNP_TRANSACTION_STATUS, "00");
            put(VNP_TNX_REF, "52_170822PK5LBW");
            put(VNP_SECURE_HASH, "2f359a2d4d40951a0846b214102d6499c03e6de9911d371be2ac4bf39276a63f72e011b21951ef4da7b5d6d853480b78a8333eaeace115ccb73a394f1d561610");
        }};

        Mockito.when(balanceDAO.findByUserId(52L)).thenReturn(Optional.of(balance));
        Mockito.when(vnPayTransactionDAO.save(Mockito.any(VnPayTransaction.class))).thenReturn(vnPayTransaction);

        // when
        RepairerDepositResponse response = underTest.responseRepairerDeposit(requestParams).getBody();

        // then
        Assertions.assertEquals(PAYMENT_SUCCESS, response.getMessage());
    }

    @Test
    void test_responseRepairerDeposit_fail_when_secure_hash_is_null() {
        // given
        Map<String, String> requestParams = new HashMap() {{
            put(VNP_AMOUNT, "3150000");
            put(VNP_BANK_CODE, "NCB");
            put(VNP_BANK_TRAN_NO, "VNP13818300");
            put(VNP_CARD_TYPE, "ATM");
            put(VNP_ORDER_INFO, "sieu+pham+2022");
            put(VNP_PAY_DATE, "20220817092228");
            put(VNP_RESPONSE_CODE, "00");
            put(VNP_TMN_CODE, "1TZDUKO5");
            put(VNP_TRANSACTION_NO, "13818300");
            put(VNP_TRANSACTION_STATUS, "00");
            put(VNP_TNX_REF, "52_170822PK5LBW");
            put(VNP_SECURE_HASH, null);
        }};

        Mockito.when(vnPayTransactionDAO.save(Mockito.any(VnPayTransaction.class))).thenReturn(vnPayTransaction);

        // when
        RepairerDepositResponse response = underTest.responseRepairerDeposit(requestParams).getBody();

        // then
        Assertions.assertEquals(INVALID_CHECKSUM, response.getMessage());
    }

    @Test
    void test_responseRepairerDeposit_fail_when_tnx_ref_is_null() {
        // given
        Map<String, String> requestParams = new HashMap() {{
            put(VNP_AMOUNT, "3150000");
            put(VNP_BANK_CODE, "NCB");
            put(VNP_BANK_TRAN_NO, "VNP13818300");
            put(VNP_CARD_TYPE, "ATM");
            put(VNP_ORDER_INFO, "sieu+pham+2022");
            put(VNP_PAY_DATE, "20220817092228");
            put(VNP_RESPONSE_CODE, "00");
            put(VNP_TMN_CODE, "1TZDUKO5");
            put(VNP_TRANSACTION_NO, "13818300");
            put(VNP_TRANSACTION_STATUS, "00");
            put(VNP_TNX_REF, null);
            put(VNP_SECURE_HASH, "017b4d8c4f28044fb7eee45476ee7be7557b436832d2812eb6fe4d07765df3666972069dad7e2221a004c750545ffc3f9c38ee3699902995cf515ff90fe73c40");
        }};

        Mockito.when(vnPayTransactionDAO.save(Mockito.any(VnPayTransaction.class))).thenReturn(vnPayTransaction);

        // when
        RepairerDepositResponse response = underTest.responseRepairerDeposit(requestParams).getBody();

        // then
        Assertions.assertEquals(VNP_TXN_REF_IS_REQUIRED, response.getMessage());
    }

    @Test
    void test_responseRepairerDeposit_fail_when_tnx_ref_is_invalid() {
        // given
        Map<String, String> requestParams = new HashMap() {{
            put(VNP_AMOUNT, "3150000");
            put(VNP_BANK_CODE, "NCB");
            put(VNP_BANK_TRAN_NO, "VNP13818300");
            put(VNP_CARD_TYPE, "ATM");
            put(VNP_ORDER_INFO, "sieu+pham+2022");
            put(VNP_PAY_DATE, "20220817092228");
            put(VNP_RESPONSE_CODE, "00");
            put(VNP_TMN_CODE, "1TZDUKO5");
            put(VNP_TRANSACTION_NO, "13818300");
            put(VNP_TRANSACTION_STATUS, "00");
            put(VNP_TNX_REF, "170822PK5LBW");
            put(VNP_SECURE_HASH, "969077a7ba3a361cb14f96f1eb956c128c90a1c1ac6ba4c0dc28f16bf3e302a632b5897f5847e8c22729b1c2af8f1200616b96c9c2f01cc49a079e7b33835c0c");
        }};

        Mockito.when(vnPayTransactionDAO.save(Mockito.any(VnPayTransaction.class))).thenReturn(vnPayTransaction);

        // when
        RepairerDepositResponse response = underTest.responseRepairerDeposit(requestParams).getBody();

        // then
        Assertions.assertEquals(WRONG_FORMAT_TXN_REF, response.getMessage());
    }

    @Test
    void test_responseRepairerDeposit_fail_when_response_code_is_not_00() {
        // given
        Map<String, String> requestParams = new HashMap() {{
            put(VNP_AMOUNT, "3150000");
            put(VNP_BANK_CODE, "NCB");
            put(VNP_BANK_TRAN_NO, "VNP13818300");
            put(VNP_CARD_TYPE, "ATM");
            put(VNP_ORDER_INFO, "sieu+pham+2022");
            put(VNP_PAY_DATE, "20220817092228");
            put(VNP_RESPONSE_CODE, "01");
            put(VNP_TMN_CODE, "1TZDUKO5");
            put(VNP_TRANSACTION_NO, "13818300");
            put(VNP_TRANSACTION_STATUS, "00");
            put(VNP_TNX_REF, "52_170822PK5LBW");
            put(VNP_SECURE_HASH, "365ba0b2657436a41ad3cb625f0fe4225cc60cdbee8925076a2503b937179e8b0f35d46b8c952adf89bfa5d8cf861f949283e6b40ded8e19285ed517f58c8739");
        }};

        Mockito.when(vnPayTransactionDAO.save(Mockito.any(VnPayTransaction.class))).thenReturn(vnPayTransaction);

        // when
        RepairerDepositResponse response = underTest.responseRepairerDeposit(requestParams).getBody();

        // then
        Assertions.assertEquals(PAYMENT_FAILED, response.getMessage());
    }

    @Test
    void test_responseRepairerDeposit_fail_when_transaction_existed() {
        // given
        Map<String, String> requestParams = new HashMap() {{
            put(VNP_AMOUNT, "3150000");
            put(VNP_BANK_CODE, "NCB");
            put(VNP_BANK_TRAN_NO, "VNP13818300");
            put(VNP_CARD_TYPE, "ATM");
            put(VNP_ORDER_INFO, "sieu+pham+2022");
            put(VNP_PAY_DATE, "20220817092228");
            put(VNP_RESPONSE_CODE, "00");
            put(VNP_TMN_CODE, "1TZDUKO5");
            put(VNP_TRANSACTION_NO, "13818300");
            put(VNP_TRANSACTION_STATUS, "00");
            put(VNP_TNX_REF, "52_170822PK5LBW");
            put(VNP_SECURE_HASH, "2f359a2d4d40951a0846b214102d6499c03e6de9911d371be2ac4bf39276a63f72e011b21951ef4da7b5d6d853480b78a8333eaeace115ccb73a394f1d561610");
        }};

        Mockito.when(vnPayTransactionDAO.save(Mockito.any(VnPayTransaction.class))).thenReturn(vnPayTransaction);
        Mockito.when(vnPayTransactionDAO.findByVnpTxnRefAndResponseCode("52_170822PK5LBW", VN_PAY_SUCCESS_CODE))
                .thenReturn(Optional.of(vnPayTransaction));

        // when
        RepairerDepositResponse response = underTest.responseRepairerDeposit(requestParams).getBody();

        // then
        Assertions.assertEquals(VNP_TXN_REF_EXISTED_IN_DATABASE, response.getMessage());
    }

    @Test
    void test_responseRepairerDeposit_success_when_amount_is_not_a_number_because_amount_convert_to_0() {
        // given
        Map<String, String> requestParams = new HashMap() {{
            put(VNP_AMOUNT, "abcd");
            put(VNP_BANK_CODE, "NCB");
            put(VNP_BANK_TRAN_NO, "VNP13818300");
            put(VNP_CARD_TYPE, "ATM");
            put(VNP_ORDER_INFO, "sieu+pham+2022");
            put(VNP_PAY_DATE, "20220817092228");
            put(VNP_RESPONSE_CODE, "00");
            put(VNP_TMN_CODE, "1TZDUKO5");
            put(VNP_TRANSACTION_NO, "13818300");
            put(VNP_TRANSACTION_STATUS, "00");
            put(VNP_TNX_REF, "52_170822PK5LBW");
            put(VNP_SECURE_HASH, "afbf6f08e53258dea3cf154c4e82747d23ed4b7ea18db943197d459effffe155c3a113cc0ea328c8284ba27977422cdadd4dca455010475cbafc3a9ee075854e");
        }};

        Mockito.when(balanceDAO.findByUserId(52L)).thenReturn(Optional.of(balance));
        Mockito.when(vnPayTransactionDAO.save(Mockito.any(VnPayTransaction.class))).thenReturn(vnPayTransaction);

        // when
        RepairerDepositResponse response = underTest.responseRepairerDeposit(requestParams).getBody();

        // then
        Assertions.assertEquals(PAYMENT_SUCCESS, response.getMessage());
    }

    @Test
    void test_hmacSHA512_fail_when_key_is_null() {
        // given
        String data = "abc";

        // when
        String result = underTest.hmacSHA512(null, data);

        // then
        Assertions.assertEquals(Strings.EMPTY, result);
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

    void setRepairerContext(Long id, String phone) {
        String[] roles = {"ROLE_REPAIRER"};
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(new UserPrincipal(id, phone, roles), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}