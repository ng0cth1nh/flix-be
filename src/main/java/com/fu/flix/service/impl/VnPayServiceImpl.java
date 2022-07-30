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
import com.fu.flix.entity.*;
import com.fu.flix.service.VNPayService;
import com.fu.flix.util.DateFormatUtil;
import com.fu.flix.util.InputValidation;
import com.fu.flix.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static com.fu.flix.constant.Constant.*;
import static com.fu.flix.constant.enums.TransactionType.*;
import static com.fu.flix.constant.enums.VnPayResult.FAIL;
import static com.fu.flix.constant.enums.VnPayResult.SUCCESS;

@Service
@Slf4j
@Transactional
public class VnPayServiceImpl implements VNPayService {
    private final RepairRequestDAO repairRequestDAO;
    private final VnPayTransactionDAO vnPayTransactionDAO;
    private final RepairRequestMatchingDAO repairRequestMatchingDAO;
    private final BalanceDAO balanceDAO;
    private final InvoiceDAO invoiceDAO;
    private final TransactionHistoryDAO transactionHistoryDAO;
    private final AppConf.VnPayInfo vnPayInfo;
    private final Integer vnPayAmountRate;
    private final RepairerDAO repairerDAO;
    private final AppConf appConf;

    private final String VN_PAY_SUCCESS_CODE = "00";
    private final String VNP_AMOUNT = "vnp_Amount";
    private final String VNP_TNX_REF = "vnp_TxnRef";
    private final String VNP_SECURE_HASH = "vnp_SecureHash";
    private final String VNP_RESPONSE_CODE = "vnp_ResponseCode";
    private final String VNP_SECURE_HASH_TYPE = "vnp_SecureHashType";
    private final String VNP_BANK_CODE = "vnp_BankCode";
    private final String VNP_BANK_TRAN_NO = "vnp_BankTranNo";
    private final String VNP_CARD_TYPE = "vnp_CardType";
    private final String VNP_ORDER_INFO = "vnp_OrderInfo";
    private final String VNP_PAY_DATE = "vnp_PayDate";
    private final String VNP_TMN_CODE = "vnp_TmnCode";
    private final String VNP_TRANSACTION_NO = "vnp_TransactionNo";
    private final String VNP_TRANSACTION_STATUS = "vnp_TransactionStatus";
    private final String VNP_VERSION = "vnp_Version";
    private final String VNP_COMMAND = "vnp_Command";
    private final String VNP_CURR_CODE = "vnp_CurrCode";
    private final String VNP_LOCALE = "vnp_Locale";
    private final String VNP_RETURN_URL = "vnp_ReturnUrl";
    private final String VNP_IP_ADDR = "vnp_IpAddr";
    private final String VNP_CREATE_DATE = "vnp_CreateDate";

    public VnPayServiceImpl(AppConf appConf,
                            RepairRequestDAO repairRequestDAO,
                            VnPayTransactionDAO vnPayTransactionDAO,
                            RepairRequestMatchingDAO repairRequestMatchingDAO,
                            BalanceDAO balanceDAO,
                            InvoiceDAO invoiceDAO,
                            TransactionHistoryDAO transactionHistoryDAO,
                            RepairerDAO repairerDAO,
                            AppConf appConf1) {
        this.repairRequestDAO = repairRequestDAO;
        this.vnPayTransactionDAO = vnPayTransactionDAO;
        this.repairRequestMatchingDAO = repairRequestMatchingDAO;
        this.balanceDAO = balanceDAO;
        this.invoiceDAO = invoiceDAO;
        this.transactionHistoryDAO = transactionHistoryDAO;
        this.vnPayInfo = appConf.getVnPayInfo();
        this.repairerDAO = repairerDAO;
        this.appConf = appConf1;
        this.vnPayAmountRate = this.vnPayInfo.getVnPayAmountRate();
    }

    @Override
    public ResponseEntity<CustomerPaymentUrlResponse> createCustomerPaymentUrl(CustomerPaymentUrlRequest customerPaymentUrlRequest,
                                                                               HttpServletRequest httpServletRequest) {
        String requestCode = customerPaymentUrlRequest.getRequestCode();
        validatePaymentInput(customerPaymentUrlRequest);
        Invoice invoice = invoiceDAO.findByRequestCode(requestCode).get();

        String vnpOrderInfo = customerPaymentUrlRequest.getOrderInfo();
        String vnpIpAddress = getIpAddress(httpServletRequest);
        int amount = invoice.getActualProceeds().intValue() * vnPayAmountRate;
        String bankCode = customerPaymentUrlRequest.getBankCode();
        String vnpTmnCode = vnPayInfo.getPaymentInfo().getTmnCode();
        String returnUrl = vnPayInfo.getPaymentInfo().getReturnUrl();
        String secureHash = vnPayInfo.getPaymentInfo().getSecureHash();

        Map<String, String> vnpParams = buildVnpParams(vnpOrderInfo, amount, bankCode, vnpIpAddress, vnpTmnCode, returnUrl, requestCode);
        CustomerPaymentUrlResponse response = new CustomerPaymentUrlResponse();
        response.setMessage(CREATE_PAYMENT_URL_SUCCESS);
        response.setData(buildPaymentUrl(vnpParams, secureHash));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<RepairerDepositUrlResponse> createRepairerDepositUrl(RepairerDepositUrlRequest repairerDepositUrlRequest,
                                                                               HttpServletRequest httpServletRequest) {
        validateDepositInput(repairerDepositUrlRequest);

        String vnpOrderInfo = repairerDepositUrlRequest.getOrderInfo();
        String vnpIpAddress = getIpAddress(httpServletRequest);
        int amount = repairerDepositUrlRequest.getAmount().intValue() * vnPayAmountRate;
        String bankCode = repairerDepositUrlRequest.getBankCode();
        String vnpTmnCode = vnPayInfo.getDepositInfo().getTmnCode();
        String returnUrl = vnPayInfo.getDepositInfo().getReturnUrl();
        String secureHash = vnPayInfo.getDepositInfo().getSecureHash();
        String txnRef = repairerDepositUrlRequest.getUserId() + "_" + RandomUtil.generateCode();

        Map<String, String> vnpParams = buildVnpParams(vnpOrderInfo, amount, bankCode, vnpIpAddress, vnpTmnCode, returnUrl, txnRef);
        RepairerDepositUrlResponse response = new RepairerDepositUrlResponse();
        response.setMessage(CREATE_PAYMENT_URL_SUCCESS);
        response.setData(buildPaymentUrl(vnpParams, secureHash));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void validatePaymentInput(CustomerPaymentUrlRequest customerPaymentUrlRequest) {
        String orderInfo = InputValidation.removeAccent(customerPaymentUrlRequest.getOrderInfo());
        customerPaymentUrlRequest.setOrderInfo(orderInfo);

        if (Strings.isEmpty(orderInfo)) {
            throw new GeneralException(HttpStatus.GONE, ORDER_INFO_IS_REQUIRED);
        }

        if (Strings.isEmpty(customerPaymentUrlRequest.getBankCode())) {
            throw new GeneralException(HttpStatus.GONE, BANK_CODE_IS_REQUIRED);
        }

        String requestCode = customerPaymentUrlRequest.getRequestCode();
        if (Strings.isEmpty(requestCode)) {
            throw new GeneralException(HttpStatus.GONE, REQUEST_CODE_IS_REQUIRED);
        }

        Optional<RepairRequest> optionalRepairRequest = repairRequestDAO
                .findByUserIdAndRequestCode(customerPaymentUrlRequest.getUserId(), requestCode);
        if (optionalRepairRequest.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_REQUEST_CODE);
        }

        RepairRequest repairRequest = optionalRepairRequest.get();
        if (!RequestStatus.PAYMENT_WAITING.getId().equals(repairRequest.getStatusId())) {
            throw new GeneralException(HttpStatus.GONE, CUSTOMER_PAYMENT_ONLY_USE_WHEN_STATUS_IS_PAYMENT_WAITING);
        }

        if (!PaymentMethod.VNPay.getId().equals(repairRequest.getPaymentMethodId())) {
            throw new GeneralException(HttpStatus.GONE, PAYMENT_METHOD_MUST_BE_VN_PAY);
        }
    }

    private void validateDepositInput(RepairerDepositUrlRequest repairerDepositUrlRequest) {
        String orderInfo = InputValidation.removeAccent(repairerDepositUrlRequest.getOrderInfo());
        repairerDepositUrlRequest.setOrderInfo(orderInfo);

        if (Strings.isEmpty(orderInfo)) {
            throw new GeneralException(HttpStatus.GONE, ORDER_INFO_IS_REQUIRED);
        }

        if (Strings.isEmpty(repairerDepositUrlRequest.getBankCode())) {
            throw new GeneralException(HttpStatus.GONE, BANK_CODE_IS_REQUIRED);
        }

        Long minVnPay = appConf.getMinVnPay();
        if (repairerDepositUrlRequest.getAmount() == null || repairerDepositUrlRequest.getAmount() < minVnPay) {
            throw new GeneralException(HttpStatus.GONE, AMOUNT_MUST_BE_GREATER_OR_EQUAL_ + minVnPay);
        }
    }

    private Map<String, String> buildVnpParams(String vnpOrderInfo,
                                               int amount,
                                               String bankCode,
                                               String vnpIpAddress,
                                               String vnpTmnCode,
                                               String returnUrl,
                                               String tnxRef) {
        LocalDateTime now = LocalDateTime.now();
        String vnpVersion = vnPayInfo.getVersion();
        String vnpCommand = vnPayInfo.getCommand();

        String locate = vnPayInfo.getLocate();
        log.info("create vnPay url success, amount: " + amount / vnPayAmountRate);

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put(VNP_VERSION, vnpVersion);
        vnpParams.put(VNP_COMMAND, vnpCommand);
        vnpParams.put(VNP_TMN_CODE, vnpTmnCode);
        vnpParams.put(VNP_AMOUNT, String.valueOf(amount));
        vnpParams.put(VNP_CURR_CODE, vnPayInfo.getCurrCode());
        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParams.put(VNP_BANK_CODE, bankCode);
        }
        vnpParams.put(VNP_TNX_REF, tnxRef);
        vnpParams.put(VNP_ORDER_INFO, vnpOrderInfo);
        vnpParams.put(VNP_LOCALE, locate);
        vnpParams.put(VNP_RETURN_URL, returnUrl);
        vnpParams.put(VNP_IP_ADDR, vnpIpAddress);
        vnpParams.put(VNP_CREATE_DATE, DateFormatUtil.toString(now, vnPayInfo.getDatePattern()));

        return vnpParams;
    }

    private String buildPaymentUrl(Map<String, String> vnpParams, String secureHash) {
        //Build data to hash and querystring
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {

                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(secureHash, hashData.toString());
        queryUrl += "&" + VNP_SECURE_HASH + "=" + vnp_SecureHash;
        return vnPayInfo.getPayUrl() + "?" + queryUrl;
    }


    @Override
    public ResponseEntity<CustomerPaymentResponse> responseCustomerPayment(Map<String, String> requestParams) {
        CustomerPaymentResponse response = new CustomerPaymentResponse();
        String vnp_SecureHash = requestParams.get(VNP_SECURE_HASH);
        requestParams.remove(VNP_SECURE_HASH_TYPE);
        requestParams.remove(VNP_SECURE_HASH);

        // Check checksum
        String signValue = hashAllFields(requestParams, vnPayInfo.getPaymentInfo().getSecureHash());
        if (!signValue.equals(vnp_SecureHash)) {
            savePaymentTransactions(requestParams, null, null, INVALID_CHECKSUM);
            response.setMessage(INVALID_CHECKSUM);
            response.setRspCode(VN_PAY_RESPONSE.get(INVALID_CHECKSUM));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        String requestCode = requestParams.get(VNP_TNX_REF);
        if (Strings.isEmpty(requestCode)) {
            savePaymentTransactions(requestParams, null, null, VNP_TXN_REF_IS_REQUIRED);
            response.setMessage(VNP_TXN_REF_IS_REQUIRED);
            response.setRspCode(VN_PAY_RESPONSE.get(VNP_TXN_REF_IS_REQUIRED));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        Optional<RepairRequest> optionalRepairRequest = repairRequestDAO.findByRequestCode(requestCode);
        if (optionalRepairRequest.isEmpty()) {
            savePaymentTransactions(requestParams, null, null, REPAIR_REQUEST_NOT_FOUND);
            response.setMessage(REPAIR_REQUEST_NOT_FOUND);
            response.setRspCode(VN_PAY_RESPONSE.get(REPAIR_REQUEST_NOT_FOUND));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        RepairRequest repairRequest = optionalRepairRequest.get();
        Long customerId = repairRequest.getUserId();
        if (!RequestStatus.PAYMENT_WAITING.getId().equals(repairRequest.getStatusId())) {
            savePaymentTransactions(requestParams, null, customerId, CUSTOMER_PAYMENT_ONLY_USE_WHEN_STATUS_IS_PAYMENT_WAITING);
            response.setMessage(CUSTOMER_PAYMENT_ONLY_USE_WHEN_STATUS_IS_PAYMENT_WAITING);
            response.setRspCode(VN_PAY_RESPONSE.get(CUSTOMER_PAYMENT_ONLY_USE_WHEN_STATUS_IS_PAYMENT_WAITING));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();
        Long repairerId = repairRequestMatching.getRepairerId();
        Repairer repairer = repairerDAO.findByUserId(repairerId).get();

        String responseCode = requestParams.get(VNP_RESPONSE_CODE);
        if (!VN_PAY_SUCCESS_CODE.equals(responseCode)) {
            savePaymentTransactions(requestParams, repairerId, customerId, PAYMENT_FAILED);
            response.setMessage(PAYMENT_FAILED);
            response.setRspCode(VN_PAY_RESPONSE.get(PAYMENT_FAILED));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        if (vnPayTransactionDAO.findByVnpTxnRefAndResponseCode(requestCode, VN_PAY_SUCCESS_CODE).isPresent()) {
            savePaymentTransactions(requestParams, repairerId, customerId, VNP_TXN_REF_EXISTED_IN_DATABASE);
            response.setMessage(VNP_TXN_REF_EXISTED_IN_DATABASE);
            response.setRspCode(VN_PAY_RESPONSE.get(VNP_TXN_REF_EXISTED_IN_DATABASE));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        Invoice invoice = invoiceDAO.findByRequestCode(requestCode).get();
        Long amount = Long.parseLong(requestParams.get(VNP_AMOUNT)) / vnPayAmountRate;
        if (!invoice.getActualProceeds().equals(amount)) {
            savePaymentTransactions(requestParams, repairerId, customerId, AMOUNT_DOES_NOT_MATCH_TO_INVOICE);
            log.info("Actual proceed: " + invoice.getActualProceeds() + ", amount: " + amount);
            response.setMessage(AMOUNT_DOES_NOT_MATCH_TO_INVOICE);
            response.setRspCode(VN_PAY_RESPONSE.get(AMOUNT_DOES_NOT_MATCH_TO_INVOICE));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        plusBalanceForRepairer(amount, repairerId);
        savePaymentTransactions(requestParams, repairerId, customerId, null);
        repairer.setRepairing(false);
        repairRequest.setStatusId(RequestStatus.DONE.getId());

        log.info("user id: " + customerId + "payment success for request " + requestCode + " success");
        response.setMessage(PAYMENT_SUCCESS);
        response.setRspCode(VN_PAY_RESPONSE.get(PAYMENT_SUCCESS));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void savePaymentTransactions(Map<String, String> requestParams,
                                         Long repairerId,
                                         Long customerId,
                                         String failReason) {
        VnPayTransaction savedVnPayTransaction = saveVnPayTransaction(requestParams);
        saveCustomerPaymentTransaction(requestParams, customerId, savedVnPayTransaction.getId(), failReason);
        saveRepairerReceivePaymentTransaction(requestParams, repairerId, failReason);
    }

    private void saveCustomerPaymentTransaction(Map<String, String> requestParams,
                                                Long customerId,
                                                Long vnPayTransactionId,
                                                String failReason) {
        TransactionHistory transactionHistory = new TransactionHistory();
        transactionHistory.setRequestCode(requestParams.get(VNP_TNX_REF));
        transactionHistory.setAmount(getTransactionAmount(requestParams));
        transactionHistory.setType(CUSTOMER_PAYMENT.name());
        transactionHistory.setUserId(customerId);
        transactionHistory.setVnpayTransactionId(vnPayTransactionId);
        transactionHistory.setStatus(Strings.isEmpty(failReason) ? SUCCESS.name() : FAIL.name());
        transactionHistory.setFailReason(failReason);
        transactionHistory.setTransactionCode(RandomUtil.generateCode());
        transactionHistoryDAO.save(transactionHistory);
    }

    private void saveRepairerReceivePaymentTransaction(Map<String, String> requestParams,
                                                       Long repairerId,
                                                       String failReason) {
        TransactionHistory transactionHistory = new TransactionHistory();
        transactionHistory.setRequestCode(requestParams.get(VNP_TNX_REF));
        transactionHistory.setAmount(getTransactionAmount(requestParams));
        transactionHistory.setType(RECEIVE_INVOICE_MONEY.name());
        transactionHistory.setUserId(repairerId);
        transactionHistory.setStatus(Strings.isEmpty(failReason) ? SUCCESS.name() : FAIL.name());
        transactionHistory.setFailReason(failReason);
        transactionHistory.setTransactionCode(RandomUtil.generateCode());
        transactionHistoryDAO.save(transactionHistory);
    }

    @Override
    public ResponseEntity<RepairerDepositResponse> responseRepairerDeposit(Map<String, String> requestParams) {
        RepairerDepositResponse response = new RepairerDepositResponse();
        String vnp_SecureHash = requestParams.get(VNP_SECURE_HASH);
        requestParams.remove(VNP_SECURE_HASH_TYPE);
        requestParams.remove(VNP_SECURE_HASH);

        // Check checksum
        String signValue = hashAllFields(requestParams, vnPayInfo.getDepositInfo().getSecureHash());
        if (!signValue.equals(vnp_SecureHash)) {
            log.info("Invalid check sum");
            log.info("sign value: " + signValue);
            log.info("secure hash: " + vnp_SecureHash);
            saveRepairerDepositTransactions(requestParams, null, INVALID_CHECKSUM);
            response.setMessage(INVALID_CHECKSUM);
            response.setRspCode(VN_PAY_RESPONSE.get(INVALID_CHECKSUM));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        String txnRef = requestParams.get(VNP_TNX_REF);
        if (Strings.isEmpty(txnRef)) {
            saveRepairerDepositTransactions(requestParams, null, VNP_TXN_REF_IS_REQUIRED);
            response.setMessage(VNP_TXN_REF_IS_REQUIRED);
            response.setRspCode(VN_PAY_RESPONSE.get(VNP_TXN_REF_IS_REQUIRED));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        Long repairerId;
        try {
            repairerId = Long.valueOf(txnRef.split("_")[0]);
        } catch (Exception e) {
            saveRepairerDepositTransactions(requestParams, null, WRONG_FORMAT_TXN_REF);
            response.setMessage(WRONG_FORMAT_TXN_REF);
            response.setRspCode(VN_PAY_RESPONSE.get(WRONG_FORMAT_TXN_REF));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        String responseCode = requestParams.get(VNP_RESPONSE_CODE);
        if (!VN_PAY_SUCCESS_CODE.equals(responseCode)) {
            saveRepairerDepositTransactions(requestParams, repairerId, PAYMENT_FAILED);
            response.setMessage(PAYMENT_FAILED);
            response.setRspCode(VN_PAY_RESPONSE.get(PAYMENT_FAILED));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        if (vnPayTransactionDAO.findByVnpTxnRefAndResponseCode(txnRef, VN_PAY_SUCCESS_CODE).isPresent()) {
            saveRepairerDepositTransactions(requestParams, repairerId, VNP_TXN_REF_EXISTED_IN_DATABASE);
            response.setMessage(VNP_TXN_REF_EXISTED_IN_DATABASE);
            response.setRspCode(VN_PAY_RESPONSE.get(VNP_TXN_REF_EXISTED_IN_DATABASE));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        Long amount = Long.parseLong(requestParams.get(VNP_AMOUNT)) / vnPayAmountRate;
        plusBalanceForRepairer(amount, repairerId);
        saveRepairerDepositTransactions(requestParams, repairerId, null);

        log.info("user id: " + repairerId + "deposit success, amount: " + amount);
        response.setMessage(PAYMENT_SUCCESS);
        response.setRspCode(VN_PAY_RESPONSE.get(PAYMENT_SUCCESS));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void plusBalanceForRepairer(Long amount, Long repairerId) {
        Balance balance = balanceDAO.findByUserId(repairerId).get();
        balance.setBalance(balance.getBalance() + amount);
    }


    private void saveRepairerDepositTransactions(Map<String, String> requestParams,
                                                 Long repairerId,
                                                 String failReason) {
        VnPayTransaction savedVnPayTransaction = saveVnPayTransaction(requestParams);
        saveRepairerDepositTransaction(requestParams, repairerId, savedVnPayTransaction.getId(), failReason);
    }

    private VnPayTransaction saveVnPayTransaction(Map<String, String> requestParams) {
        VnPayTransaction vnPayTransaction = new VnPayTransaction();
        vnPayTransaction.setAmount(getTransactionAmount(requestParams));
        vnPayTransaction.setBankCode(requestParams.get(VNP_BANK_CODE));
        vnPayTransaction.setBankTranNo(requestParams.get(VNP_BANK_TRAN_NO));
        vnPayTransaction.setCardType(requestParams.get(VNP_CARD_TYPE));
        vnPayTransaction.setOrderInfo(requestParams.get(VNP_ORDER_INFO));
        vnPayTransaction.setPayDate(requestParams.get(VNP_PAY_DATE));
        vnPayTransaction.setResponseCode(requestParams.get(VNP_RESPONSE_CODE));
        vnPayTransaction.setTmnCode(requestParams.get(VNP_TMN_CODE));
        vnPayTransaction.setTransactionNo(requestParams.get(VNP_TRANSACTION_NO));
        vnPayTransaction.setTransactionStatus(requestParams.get(VNP_TRANSACTION_STATUS));
        vnPayTransaction.setVnpTxnRef(requestParams.get(VNP_TNX_REF));
        vnPayTransaction.setSecureHash(requestParams.get(VNP_SECURE_HASH));
        return vnPayTransactionDAO.save(vnPayTransaction);
    }

    private void saveRepairerDepositTransaction(Map<String, String> requestParams,
                                                Long repairerId,
                                                Long vnPayTransactionId,
                                                String failReason) {
        TransactionHistory transactionHistory = new TransactionHistory();
        transactionHistory.setAmount(getTransactionAmount(requestParams));
        transactionHistory.setType(DEPOSIT.name());
        transactionHistory.setUserId(repairerId);
        transactionHistory.setVnpayTransactionId(vnPayTransactionId);
        transactionHistory.setStatus(Strings.isEmpty(failReason) ? SUCCESS.name() : FAIL.name());
        transactionHistory.setFailReason(failReason);
        transactionHistory.setTransactionCode(RandomUtil.generateCode());
        transactionHistoryDAO.save(transactionHistory);
    }

    private Long getTransactionAmount(Map<String, String> requestParams) {
        try {
            return Long.parseLong(requestParams.get(VNP_AMOUNT)) / vnPayAmountRate;
        } catch (Exception e) {
            return 0L;
        }
    }

    private String getIpAddress(HttpServletRequest httpServletRequest) {
        final String HEADER = "X-FORWARDED-FOR";
        String ipAddress;
        try {
            ipAddress = httpServletRequest.getHeader(HEADER);
            if (ipAddress == null) {
                ipAddress = httpServletRequest.getRemoteAddr();
            }
        } catch (Exception e) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, INVALID_IP_ADDRESS);
        }
        return ipAddress;
    }

    private String hashAllFields(Map<String, String> fields, String secureHash) {
        // create a list and sort it
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        // create a buffer for the md5 input and add the secure secret first
        StringBuilder sb = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(replaceAllWhiteSpaceToPlusCharacter(fieldValue));
            }
            if (itr.hasNext()) {
                sb.append("&");
            }
        }
        return hmacSHA512(secureHash, sb.toString());
    }

    private String replaceAllWhiteSpaceToPlusCharacter(String s) {
        return s.replace(' ', '+');
    }

    private String hmacSHA512(final String key, final String data) {
        final String HMAC_SHA512 = "HmacSHA512";
        try {
            if (key == null || data == null) {
                throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, KEY_AND_DATA_FOR_HMAC_SHA512_IS_REQUIRED);
            }
            final Mac hmac512 = Mac.getInstance(HMAC_SHA512);
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, HMAC_SHA512);
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception ex) {
            return "";
        }
    }
}
