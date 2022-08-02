package com.fu.flix.service.impl;

import com.fu.flix.dao.CommentDAO;
import com.fu.flix.dao.RepairRequestDAO;
import com.fu.flix.dao.UserAddressDAO;
import com.fu.flix.dto.UserAddressDTO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.dto.security.UserPrincipal;
import com.fu.flix.entity.User;
import com.fu.flix.entity.UserAddress;
import com.fu.flix.service.CustomerService;
import com.fu.flix.service.RepairerService;
import com.fu.flix.service.ValidatorService;
import com.fu.flix.util.DateFormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.fu.flix.constant.Constant.*;
import static com.fu.flix.constant.enums.RequestStatus.PENDING;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Slf4j
class CustomerServiceImplTest {

    @Autowired
    CustomerService underTest;

    @Autowired
    RepairRequestDAO repairRequestDAO;
    @Autowired
    UserAddressDAO userAddressDAO;
    @Autowired
    ValidatorService validatorService;
    String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    String DATE_PATTERN = "dd-MM-yyyy";

    @Autowired
    CommentDAO commentDAO;

    @Autowired
    RepairerService repairerService;

    @Test
    public void test_create_fixing_request_success() {
        // given
        Long serviceId = 1L;
        Long addressId = 7L;
        String expectFixingDay = DateFormatUtil.toString(LocalDateTime.now().plusDays(2L), DATE_TIME_PATTERN);
        String description = "Thợ phải đẹp trai";
        Long voucherId = 1L;
        String paymentMethodId = "C";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setVoucherId(voucherId);
        request.setDescription(description);
        request.setExpectFixingDay(expectFixingDay);
        request.setAddressId(addressId);
        request.setPaymentMethodId(paymentMethodId);

        setUserContext(36L, "0865390037");

        // when
        RequestingRepairResponse response = underTest.createFixingRequest(request).getBody();

        // then
        Assertions.assertEquals(CREATE_REPAIR_REQUEST_SUCCESSFUL, response.getMessage());
        Assertions.assertEquals(PENDING.name(), response.getStatus());
    }

    @Test
    public void test_create_fixing_request_fail_when_wrong_address_id() {
        // given
        Long serviceId = 1L;
        Long addressId = 0L;
        String expectFixingDay = DateFormatUtil.toString(LocalDateTime.now().plusDays(2L), DATE_TIME_PATTERN);
        String description = "Thợ phải đẹp trai";
        Long voucherId = 1L;
        String paymentMethodId = "C";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setVoucherId(voucherId);
        request.setDescription(description);
        request.setExpectFixingDay(expectFixingDay);
        request.setAddressId(addressId);
        request.setPaymentMethodId(paymentMethodId);

        setUserContext(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFixingRequest(request));

        // then
        Assertions.assertEquals(INVALID_ADDRESS, exception.getMessage());
    }

    @Test
    public void test_create_fixing_request_fail_when_address_id_is_null() {
        // given
        Long serviceId = 1L;
        String expectFixingDay = DateFormatUtil.toString(LocalDateTime.now().plusDays(2L), DATE_TIME_PATTERN);
        String description = "Thợ phải đẹp trai";
        Long voucherId = 1L;
        String paymentMethodId = "C";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setVoucherId(voucherId);
        request.setDescription(description);
        request.setExpectFixingDay(expectFixingDay);
        request.setAddressId(null);
        request.setPaymentMethodId(paymentMethodId);
        setUserContext(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFixingRequest(request));

        // then
        Assertions.assertEquals(INVALID_ADDRESS, exception.getMessage());
    }

    @Test
    public void test_create_fixing_request_fail_when_expect_fixing_day_is_empty() {
        // given
        Long serviceId = 1L;
        Long addressId = 0L;
        String expectFixingDay = "";
        String description = "Thợ phải đẹp trai";
        Long voucherId = 1L;
        String paymentMethodId = "C";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setVoucherId(voucherId);
        request.setDescription(description);
        request.setExpectFixingDay(expectFixingDay);
        request.setAddressId(addressId);
        request.setPaymentMethodId(paymentMethodId);

        setUserContext(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFixingRequest(request));

        // then
        Assertions.assertEquals(WRONG_LOCAL_DATE_TIME_FORMAT, exception.getMessage());
    }

    @Test
    public void test_create_fixing_request_fail_when_expect_fixing_day_is_null() {
        // given
        Long serviceId = 1L;
        Long addressId = 0L;
        String description = "Thợ phải đẹp trai";
        Long voucherId = 1L;
        String paymentMethodId = "C";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setVoucherId(voucherId);
        request.setDescription(description);
        request.setExpectFixingDay(null);
        request.setAddressId(addressId);
        request.setPaymentMethodId(paymentMethodId);

        setUserContext(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFixingRequest(request));

        // then
        Assertions.assertEquals(EXPECT_FIXING_DAY_IS_REQUIRED, exception.getMessage());
    }

    @Test
    public void test_create_fixing_request_fail_when_payment_method_is_not_valid_for_voucher() {
        // given
        Long serviceId = 1L;
        Long addressId = 7L;
        String expectFixingDay = DateFormatUtil.toString(LocalDateTime.now().plusDays(2L), DATE_TIME_PATTERN);
        String description = "Thợ phải đẹp trai";
        Long voucherId = 1L;
        String paymentMethodId = "V";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setVoucherId(voucherId);
        request.setDescription(description);
        request.setExpectFixingDay(expectFixingDay);
        request.setAddressId(addressId);
        request.setPaymentMethodId(paymentMethodId);
        setUserContext(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFixingRequest(request));

        // then
        Assertions.assertEquals(PAYMENT_METHOD_NOT_VALID_FOR_THIS_VOUCHER, exception.getMessage());
    }

    @Test
    public void test_create_fixing_request_success_when_description_and_voucher_are_null_and_payment_method_is_VNPAY() {
        // given
        Long serviceId = 1L;
        Long addressId = 7L;
        String expectFixingDay = DateFormatUtil.toString(LocalDateTime.now().plusDays(2L), DATE_TIME_PATTERN);
        String paymentMethodId = "V";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setVoucherId(null);
        request.setDescription(null);
        request.setExpectFixingDay(expectFixingDay);
        request.setAddressId(addressId);
        request.setPaymentMethodId(paymentMethodId);
        setUserContext(36L, "0865390037");

        // when
        RequestingRepairResponse response = underTest.createFixingRequest(request).getBody();

        // then
        Assertions.assertEquals(CREATE_REPAIR_REQUEST_SUCCESSFUL, response.getMessage());
        Assertions.assertEquals(PENDING.name(), response.getStatus());
    }

    @Test
    public void test_create_fixing_request_fail_when_invalid_voucher_id() {
        // given
        Long serviceId = 1L;
        Long addressId = 0L;
        String expectFixingDay = DateFormatUtil.toString(LocalDateTime.now().plusDays(2L), DATE_TIME_PATTERN);
        String description = "Thợ phải đẹp trai";
        Long voucherId = 0L;
        String paymentMethodId = "C";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setVoucherId(voucherId);
        request.setDescription(description);
        request.setExpectFixingDay(expectFixingDay);
        request.setAddressId(addressId);
        request.setPaymentMethodId(paymentMethodId);

        setUserContext(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFixingRequest(request));

        // then
        Assertions.assertEquals(INVALID_VOUCHER, exception.getMessage());
    }

    @Test
    public void test_create_fixing_request_fail_when_invalid_payment_method_id() {
        // given
        Long serviceId = 1L;
        Long addressId = 0L;
        String expectFixingDay = DateFormatUtil.toString(LocalDateTime.now().plusDays(2L), DATE_TIME_PATTERN);
        String description = "Thợ phải đẹp trai";
        Long voucherId = 1L;
        String paymentMethodId = "a";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setVoucherId(voucherId);
        request.setDescription(description);
        request.setExpectFixingDay(expectFixingDay);
        request.setAddressId(addressId);
        request.setPaymentMethodId(paymentMethodId);

        setUserContext(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFixingRequest(request));

        // then
        Assertions.assertEquals(INVALID_PAYMENT_METHOD, exception.getMessage());
    }

    @Test
    public void test_create_fixing_request_fail_when_payment_method_id_is_empty() {
        // given
        Long serviceId = 1L;
        Long addressId = 0L;
        String expectFixingDay = DateFormatUtil.toString(LocalDateTime.now().plusDays(2L), DATE_TIME_PATTERN);
        String description = "Thợ phải đẹp trai";
        Long voucherId = 1L;
        String paymentMethodId = "";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setVoucherId(voucherId);
        request.setDescription(description);
        request.setExpectFixingDay(expectFixingDay);
        request.setAddressId(addressId);
        request.setPaymentMethodId(paymentMethodId);

        setUserContext(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFixingRequest(request));

        // then
        Assertions.assertEquals(INVALID_PAYMENT_METHOD, exception.getMessage());
    }

    @Test
    public void test_create_fixing_request_fail_when_payment_method_id_is_null() {
        // given
        Long serviceId = 1L;
        Long addressId = 0L;
        String expectFixingDay = DateFormatUtil.toString(LocalDateTime.now().plusDays(2L), DATE_TIME_PATTERN);
        String description = "Thợ phải đẹp trai";
        Long voucherId = 1L;

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setVoucherId(voucherId);
        request.setDescription(description);
        request.setExpectFixingDay(expectFixingDay);
        request.setAddressId(addressId);
        request.setPaymentMethodId(null);

        setUserContext(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFixingRequest(request));

        // then
        Assertions.assertEquals(INVALID_PAYMENT_METHOD, exception.getMessage());
    }

    @Test
    public void test_create_fixing_request_fail_when_invalid_service_id() {
        // given
        Long serviceId = 0L;
        Long addressId = 0L;
        String expectFixingDay = DateFormatUtil.toString(LocalDateTime.now().plusDays(2L), DATE_TIME_PATTERN);
        String description = "Thợ phải đẹp trai";
        Long voucherId = 1L;
        String paymentMethodId = "C";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setVoucherId(voucherId);
        request.setDescription(description);
        request.setExpectFixingDay(expectFixingDay);
        request.setAddressId(addressId);
        request.setPaymentMethodId(paymentMethodId);

        setUserContext(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFixingRequest(request));

        // then
        Assertions.assertEquals(INVALID_SERVICE, exception.getMessage());
    }

    @Test
    public void test_create_fixing_request_fail_when_service_id_is_null() {
        // given
        Long addressId = 0L;
        String expectFixingDay = DateFormatUtil.toString(LocalDateTime.now().plusDays(2L), DATE_TIME_PATTERN);
        String description = "Thợ phải đẹp trai";
        Long voucherId = 1L;
        String paymentMethodId = "C";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(null);
        request.setVoucherId(voucherId);
        request.setDescription(description);
        request.setExpectFixingDay(expectFixingDay);
        request.setAddressId(addressId);
        request.setPaymentMethodId(paymentMethodId);

        setUserContext(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFixingRequest(request));

        // then
        Assertions.assertEquals(INVALID_SERVICE, exception.getMessage());
    }

    @Test
    public void test_create_fixing_request_fail_when_fixing_day_is_more_than_today_30_days() {
        // given
        Long serviceId = 1L;
        Long addressId = 7L;
        String expectFixingDay = DateFormatUtil.toString(LocalDateTime.now().plusDays(31L), DATE_TIME_PATTERN);
        String description = "Thợ phải đẹp trai";
        Long voucherId = 1L;
        String paymentMethodId = "C";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setVoucherId(voucherId);
        request.setDescription(description);
        request.setExpectFixingDay(expectFixingDay);
        request.setAddressId(addressId);
        request.setPaymentMethodId(paymentMethodId);

        setUserContext(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFixingRequest(request));

        // then
        Assertions.assertEquals(EXPECT_FIXING_DAY_MUST_START_AFTER_1_HOURS_AND_BEFORE_30_DAYS, exception.getMessage());
    }

    @Test
    public void test_create_fixing_request_fail_when_fixing_day_is_less_than_now_plus_1_hour() {
        // given
        Long serviceId = 1L;
        Long addressId = 0L;
        String expectFixingDay = DateFormatUtil.toString(LocalDateTime.now().plusMinutes(59), DATE_TIME_PATTERN);
        String description = "Thợ phải đẹp trai";
        Long voucherId = 1L;
        String paymentMethodId = "C";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setVoucherId(voucherId);
        request.setDescription(description);
        request.setExpectFixingDay(expectFixingDay);
        request.setAddressId(addressId);
        request.setPaymentMethodId(paymentMethodId);

        setUserContext(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFixingRequest(request));

        // then
        Assertions.assertEquals(EXPECT_FIXING_DAY_MUST_START_AFTER_1_HOURS_AND_BEFORE_30_DAYS, exception.getMessage());
    }

    @Test
    public void test_create_fixing_request_fail_when_description_length_is_2501() {
        // given
        Long serviceId = 1L;
        Long addressId = 0L;
        String expectFixingDay = DateFormatUtil.toString(LocalDateTime.now().plusDays(2), DATE_TIME_PATTERN);
        Long voucherId = 1L;
        String paymentMethodId = "C";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setVoucherId(voucherId);
        request.setDescription("t".repeat(2501));
        request.setExpectFixingDay(expectFixingDay);
        request.setAddressId(addressId);
        request.setPaymentMethodId(paymentMethodId);

        setUserContext(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFixingRequest(request));

        // then
        Assertions.assertEquals(EXCEEDED_DESCRIPTION_LENGTH_ALLOWED, exception.getMessage());
    }

    @Test
    public void test_cancel_PENDING_request_success() {
        // given
        String requestCode = createFixingRequest(36L, "0865390037");
        String reason = "Thợ không đẹp trai";
        CancelRequestForCustomerRequest request = new CancelRequestForCustomerRequest();
        request.setRequestCode(requestCode);
        request.setReason(reason);

        // when
        setUserContext(36L, "0865390037");
        CancelRequestForCustomerResponse response = underTest.cancelFixingRequest(request).getBody();

        // then
        Assertions.assertEquals(CANCEL_REPAIR_REQUEST_SUCCESSFUL, response.getMessage());
    }

    @Test
    public void test_cancel_request_success_when_reason_is_null() {
        // given
        String requestCode = createFixingRequest(36L, "0865390037");
        CancelRequestForCustomerRequest request = new CancelRequestForCustomerRequest();
        request.setRequestCode(requestCode);
        request.setReason(null);

        // when
        setUserContext(36L, "0865390037");
        CancelRequestForCustomerResponse response = underTest.cancelFixingRequest(request).getBody();

        // then
        Assertions.assertEquals(CANCEL_REPAIR_REQUEST_SUCCESSFUL, response.getMessage());
    }

    @Test
    public void test_cancel_request_success_when_reason_is_empty() {
        // given
        String requestCode = createFixingRequest(36L, "0865390037");
        CancelRequestForCustomerRequest request = new CancelRequestForCustomerRequest();
        request.setRequestCode(requestCode);
        request.setReason("");

        // when
        setUserContext(36L, "0865390037");
        CancelRequestForCustomerResponse response = underTest.cancelFixingRequest(request).getBody();

        // then
        Assertions.assertEquals(CANCEL_REPAIR_REQUEST_SUCCESSFUL, response.getMessage());
    }

    @Test
    public void test_cancel_APPROVED_request_success() {
        // given
        String requestCode = createFixingRequest(36L, "0865390037");
        approvalRequest(requestCode);
        String reason = "Thợ không đẹp trai";
        CancelRequestForCustomerRequest request = new CancelRequestForCustomerRequest();
        request.setRequestCode(requestCode);
        request.setReason(reason);

        // when
        setUserContext(36L, "0865390037");
        CancelRequestForCustomerResponse response = underTest.cancelFixingRequest(request).getBody();

        // then
        Assertions.assertEquals(CANCEL_REPAIR_REQUEST_SUCCESSFUL, response.getMessage());
    }

    @Test
    public void test_cancel_FIXING_request_fail() {
        // given
        String requestCode = createFixingRequest(36L, "0865390037");
        approvalRequest(requestCode);
        confirmFixing(requestCode);

        String reason = "Thợ không đẹp trai";
        CancelRequestForCustomerRequest request = new CancelRequestForCustomerRequest();
        request.setRequestCode(requestCode);
        request.setReason(reason);

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.cancelFixingRequest(request));

        // then
        Assertions.assertEquals(ONLY_CAN_CANCEL_REQUEST_PENDING_OR_APPROVED, exception.getMessage());
    }

    private void approvalRequest(String requestCode) {
        setUserContext(56L, "0865390056");
        RepairerApproveRequest request = new RepairerApproveRequest();
        request.setRequestCode(requestCode);
        repairerService.approveRequest(request);
    }

    private void confirmFixing(String requestCode) {
        setUserContext(56L, "0865390056");
        ConfirmFixingRequest request = new ConfirmFixingRequest();
        request.setRequestCode(requestCode);
        repairerService.confirmFixing(request);
    }

    private String createFixingRequest(Long userId, String phone) {
        setUserContext(userId, phone);
        Long serviceId = 1L;
        Long addressId = 7L;
        String expectFixingDay = DateFormatUtil.toString(LocalDateTime.now().plusDays(2L), DATE_TIME_PATTERN);
        String description = "Thợ phải đẹp trai";
        Long voucherId = 1L;
        String paymentMethodId = "C";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setVoucherId(voucherId);
        request.setDescription(description);
        request.setExpectFixingDay(expectFixingDay);
        request.setAddressId(addressId);
        request.setPaymentMethodId(paymentMethodId);

        RequestingRepairResponse response = underTest.createFixingRequest(request).getBody();
        return response.getRequestCode();
    }

    @Test
    public void test_cancel_request_fail_when_request_code_is_empty() {
        // given
        String requestCode = "";
        String reason = "Thợ không đẹp trai";
        CancelRequestForCustomerRequest request = new CancelRequestForCustomerRequest();
        request.setRequestCode(requestCode);
        request.setReason(reason);

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.cancelFixingRequest(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_cancel_request_fail_when_request_code_is_null() {
        // given
        String reason = "Thợ không đẹp trai";
        CancelRequestForCustomerRequest request = new CancelRequestForCustomerRequest();
        request.setRequestCode(null);
        request.setReason(reason);

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.cancelFixingRequest(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_getFixingRequestHistories_success_when_status_is_CANCELLED() {
        // given
        String status = "CANCELLED";
        HistoryRequestForCustomerRequest request = new HistoryRequestForCustomerRequest();
        request.setStatus(status);

        // when
        setUserContext(36L, "0865390037");
        HistoryRequestForCustomerResponse response = underTest.getFixingRequestHistories(request).getBody();

        // then
        Assertions.assertTrue(response.getRequestHistories().size() > 0);
    }

    @Test
    public void test_getFixingRequestHistories_success_when_status_is_DONE() {
        // given
        String status = "DONE";
        HistoryRequestForCustomerRequest request = new HistoryRequestForCustomerRequest();
        request.setStatus(status);

        // when
        setUserContext(36L, "0865390037");
        HistoryRequestForCustomerResponse response = underTest.getFixingRequestHistories(request).getBody();

        // then
        Assertions.assertTrue(response.getRequestHistories().size() > 0);
    }

    @Test
    public void test_getFixingRequestHistories_success_when_status_is_PENDING() {
        // given
        String status = "PENDING";
        HistoryRequestForCustomerRequest request = new HistoryRequestForCustomerRequest();
        request.setStatus(status);

        // when
        setUserContext(36L, "0865390037");
        HistoryRequestForCustomerResponse response = underTest.getFixingRequestHistories(request).getBody();

        // then
        Assertions.assertNotNull(response.getRequestHistories());
    }

    @Test
    public void test_getFixingRequestHistories_success_when_status_is_PAYMENT_WAITING() {
        // given
        String status = "PAYMENT_WAITING";
        HistoryRequestForCustomerRequest request = new HistoryRequestForCustomerRequest();
        request.setStatus(status);

        // when
        setUserContext(36L, "0865390037");
        HistoryRequestForCustomerResponse response = underTest.getFixingRequestHistories(request).getBody();

        // then
        Assertions.assertNotNull(response.getRequestHistories());
    }

    @Test
    public void test_getFixingRequestHistories_fail_when_status_is_empty() {
        // given
        String status = "";
        HistoryRequestForCustomerRequest request = new HistoryRequestForCustomerRequest();
        request.setStatus(status);

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getFixingRequestHistories(request));

        // then
        Assertions.assertEquals(INVALID_STATUS, exception.getMessage());
    }

    @Test
    public void test_getFixingRequestHistories_fail_when_status_is_null() {
        // given
        HistoryRequestForCustomerRequest request = new HistoryRequestForCustomerRequest();
        request.setStatus(null);

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getFixingRequestHistories(request));

        // then
        Assertions.assertEquals(INVALID_STATUS, exception.getMessage());
    }

    @Test
    public void test_getDetailFixingRequest_success() {
        // given
        RequestingDetailForCustomerRequest request = new RequestingDetailForCustomerRequest();
        String requestCode = "PZ32QDKGDWO4";
        request.setRequestCode(requestCode);

        // when
        setUserContext(36L, "0865390037");
        RequestingDetailForCustomerResponse response = underTest.getDetailFixingRequest(request).getBody();

        // then
        Assertions.assertEquals(requestCode, response.getRequestCode());
        Assertions.assertEquals("Tivi", response.getServiceName());
    }

    @Test
    public void test_getDetailFixingRequest_fail_when_request_code_is_invalid() {
        // given
        RequestingDetailForCustomerRequest request = new RequestingDetailForCustomerRequest();
        String requestCode = "TEST123";
        request.setRequestCode(requestCode);

        // when
        setUserContext(36L, "0865390037");
        RequestingDetailForCustomerResponse response = underTest.getDetailFixingRequest(request).getBody();

        // then
        Assertions.assertNull(response.getServiceName());
        Assertions.assertNull(response.getActualPrice());
        Assertions.assertNull(response.getCustomerName());
        Assertions.assertNull(response.getRequestCode());
        Assertions.assertNull(response.getTotalPrice());
        Assertions.assertNull(response.getDate());
    }

    @Test
    public void test_getDetailFixingRequest_fail_when_request_code_is_empty() {
        // given
        RequestingDetailForCustomerRequest request = new RequestingDetailForCustomerRequest();
        String requestCode = "";
        request.setRequestCode(requestCode);

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getDetailFixingRequest(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_getDetailFixingRequest_fail_when_request_code_is_null() {
        // given
        RequestingDetailForCustomerRequest request = new RequestingDetailForCustomerRequest();
        request.setRequestCode(null);

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getDetailFixingRequest(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_get_main_address_success() {
        // given
        MainAddressRequest request = new MainAddressRequest();

        // when
        setUserContext(36L, "0865390037");
        MainAddressResponse response = underTest.getMainAddress(request).getBody();

        // then
        Assertions.assertEquals("Nha tho duc ba, Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội", response.getAddressName());
        Assertions.assertEquals("0969696969", response.getPhone());
        Assertions.assertEquals(7L, response.getAddressId());
        Assertions.assertEquals("Faker", response.getCustomerName());
    }

    @Test
    public void test_get_customer_addresses_success() {
        // given
        UserAddressRequest request = new UserAddressRequest();

        // when
        setUserContext(36L, "0865390037");
        UserAddressResponse response = underTest.getCustomerAddresses(request).getBody();
        List<UserAddressDTO> addresses = response.getAddresses();

        // then
        Assertions.assertNotNull(addresses);
        Assertions.assertEquals("Nha tho duc ba, Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội", addresses.get(0).getAddressName());
        Assertions.assertEquals("0969696969", addresses.get(0).getPhone());
        Assertions.assertEquals(7L, addresses.get(0).getAddressId());
        Assertions.assertEquals("Faker", addresses.get(0).getCustomerName());
    }

    @Test
    public void test_deleteCustomerAddress_success() {
        // given
        DeleteAddressRequest request = new DeleteAddressRequest();
        request.setAddressId(21L);

        // when
        setUserContext(36L, "0865390037");
        DeleteAddressResponse response = underTest.deleteCustomerAddress(request).getBody();

        // then
        Assertions.assertEquals(DELETE_ADDRESS_SUCCESS, response.getMessage());
    }

    @Test
    public void test_deleteCustomerAddress_fail_when_wrong_address_id() {
        // given
        DeleteAddressRequest request = new DeleteAddressRequest();
        request.setAddressId(0L);

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.deleteCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_ADDRESS, exception.getMessage());
    }

    @Test
    public void test_deleteCustomerAddress_fail_when_try_to_delete_main_address() {
        // given
        DeleteAddressRequest request = new DeleteAddressRequest();
        request.setAddressId(7L);

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.deleteCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_ADDRESS, exception.getMessage());
    }

    @Test
    public void test_deleteCustomerAddress_fail_when_address_id_is_null() {
        // given
        DeleteAddressRequest request = new DeleteAddressRequest();
        request.setAddressId(null);

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.deleteCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_ADDRESS, exception.getMessage());
    }

    @Test
    public void test_edit_customer_address_success() {
        // given
        EditAddressRequest request = new EditAddressRequest();
        request.setPhone("0865390031");
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setCommuneId("00006");
        request.setAddressId(21L);
        request.setName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        EditAddressResponse response = underTest.editCustomerAddress(request).getBody();

        // then
        Assertions.assertEquals(EDIT_ADDRESS_SUCCESS, response.getMessage());
        Assertions.assertEquals(21L, response.getAddressId());
    }

    @Test
    public void test_edit_customer_address_fail_when_street_address_is_null() {
        // given
        EditAddressRequest request = new EditAddressRequest();
        request.setPhone("0865390031");
        request.setStreetAddress(null);
        request.setCommuneId("00006");
        request.setAddressId(21L);
        request.setName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.editCustomerAddress(request));

        // then
        Assertions.assertEquals(STREET_ADDRESS_IS_REQUIRED, exception.getMessage());
    }

    @Test
    public void test_edit_customer_address_fail_when_street_address_is_empty() {
        // given
        EditAddressRequest request = new EditAddressRequest();
        request.setPhone("0865390031");
        request.setStreetAddress("");
        request.setCommuneId("00006");
        request.setAddressId(21L);
        request.setName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.editCustomerAddress(request));

        // then
        Assertions.assertEquals(STREET_ADDRESS_IS_REQUIRED, exception.getMessage());
    }

    @Test
    public void test_edit_customer_address_fail_when_commune_id_is_empty() {
        // given
        EditAddressRequest request = new EditAddressRequest();
        request.setPhone("0865390031");
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setCommuneId("");
        request.setAddressId(21L);
        request.setName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.editCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_COMMUNE, exception.getMessage());
    }

    @Test
    public void test_edit_customer_address_fail_when_commune_id_is_null() {
        // given
        EditAddressRequest request = new EditAddressRequest();
        request.setPhone("0865390031");
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setCommuneId(null);
        request.setAddressId(21L);
        request.setName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.editCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_COMMUNE, exception.getMessage());
    }

    @Test
    public void test_edit_customer_address_fail_when_commune_id_is_wrong() {
        // given
        EditAddressRequest request = new EditAddressRequest();
        request.setPhone("0865390031");
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setCommuneId("abcde");
        request.setAddressId(21L);
        request.setName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.editCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_COMMUNE, exception.getMessage());
    }

    @Test
    public void test_edit_customer_address_fail_when_commune_id_is_0() {
        // given
        EditAddressRequest request = new EditAddressRequest();
        request.setPhone("0865390031");
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setCommuneId("0");
        request.setAddressId(21L);
        request.setName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.editCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_COMMUNE, exception.getMessage());
    }

    @Test
    public void test_edit_customer_address_fail_when_phone_is_shorter_than_required() {
        // given
        EditAddressRequest request = new EditAddressRequest();
        request.setPhone("08653900");
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setCommuneId("00006");
        request.setAddressId(21L);
        request.setName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.editCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_edit_customer_address_fail_when_phone_is_null() {
        // given
        EditAddressRequest request = new EditAddressRequest();
        request.setPhone(null);
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setCommuneId("00006");
        request.setAddressId(21L);
        request.setName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.editCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_edit_customer_address_fail_when_phone_is_longer_than_required() {
        // given
        EditAddressRequest request = new EditAddressRequest();
        request.setPhone("08653900311");
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setCommuneId("00006");
        request.setAddressId(21L);
        request.setName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.editCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_edit_customer_address_fail_when_phone_is_abc() {
        // given
        EditAddressRequest request = new EditAddressRequest();
        request.setPhone("abc");
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setCommuneId("00006");
        request.setAddressId(21L);
        request.setName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.editCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }


    @Test
    public void test_edit_customer_address_fail_when_phone_is_empty() {
        // given
        EditAddressRequest request = new EditAddressRequest();
        request.setPhone("");
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setCommuneId("00006");
        request.setAddressId(21L);
        request.setName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.editCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_edit_customer_address_success_when_name_does_not_trim() {
        // given
        EditAddressRequest request = new EditAddressRequest();
        request.setPhone("0865390031");
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setCommuneId("00006");
        request.setAddressId(21L);
        request.setName("  Nhung Nguyễn   ");

        // when
        setUserContext(36L, "0865390037");
        EditAddressResponse response = underTest.editCustomerAddress(request).getBody();

        // then
        Assertions.assertEquals(EDIT_ADDRESS_SUCCESS, response.getMessage());
        Assertions.assertEquals(21L, response.getAddressId());
    }

    @Test
    public void test_edit_customer_address_fail_when_name_is_empty() {
        // given
        EditAddressRequest request = new EditAddressRequest();
        request.setPhone("0865390031");
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setCommuneId("00006");
        request.setAddressId(21L);
        request.setName("");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.editCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_NAME, exception.getMessage());
    }

    @Test
    public void test_edit_customer_address_fail_when_name_is_null() {
        // given
        EditAddressRequest request = new EditAddressRequest();
        request.setPhone("0865390031");
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setCommuneId("00006");
        request.setAddressId(21L);
        request.setName(null);

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.editCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_NAME, exception.getMessage());
    }

    @Test
    public void test_edit_customer_address_fail_when_name_contain_special_character() {
        // given
        EditAddressRequest request = new EditAddressRequest();
        request.setPhone("0865390031");
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setCommuneId("00006");
        request.setAddressId(21L);
        request.setName("Nhung @123");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.editCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_NAME, exception.getMessage());
    }

    @Test
    public void test_edit_customer_address_fail_when_address_id_is_wrong() {
        // given
        EditAddressRequest request = new EditAddressRequest();
        request.setPhone("0865390031");
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setCommuneId("00006");
        request.setAddressId(0L);
        request.setName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.editCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_ADDRESS, exception.getMessage());
    }

    @Test
    public void test_edit_customer_address_fail_when_address_id_is_null() {
        // given
        EditAddressRequest request = new EditAddressRequest();
        request.setPhone("0865390031");
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setCommuneId("00006");
        request.setAddressId(null);
        request.setName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.editCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_ADDRESS, exception.getMessage());
    }

    @Test
    public void test_create_customer_address_success() {
        // given
        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setPhone("0975944854");
        request.setCommuneId("00006");
        request.setFullName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        CreateAddressResponse response = underTest.createCustomerAddress(request).getBody();

        // then
        log.info("Address id: " + response.getAddressId());
        Assertions.assertEquals(CREATE_ADDRESS_SUCCESS, response.getMessage());
    }

    @Test
    public void test_create_customer_address_fail_when_street_address_is_null() {
        // given
        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreetAddress(null);
        request.setPhone("0975944854");
        request.setCommuneId("00006");
        request.setFullName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createCustomerAddress(request));

        // then
        Assertions.assertEquals(STREET_ADDRESS_IS_REQUIRED, exception.getMessage());
    }

    @Test
    public void test_create_customer_address_fail_when_street_address_is_empty() {
        // given
        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreetAddress("");
        request.setPhone("0975944854");
        request.setCommuneId("00006");
        request.setFullName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createCustomerAddress(request));

        // then
        Assertions.assertEquals(STREET_ADDRESS_IS_REQUIRED, exception.getMessage());
    }

    @Test
    public void test_create_customer_address_fail_when_commune_id_is_empty() {
        // given
        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setPhone("0975944854");
        request.setCommuneId("");
        request.setFullName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_COMMUNE, exception.getMessage());
    }

    @Test
    public void test_create_customer_address_fail_when_commune_id_is_null() {
        // given
        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setPhone("0975944854");
        request.setCommuneId(null);
        request.setFullName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_COMMUNE, exception.getMessage());
    }

    @Test
    public void test_create_customer_address_fail_when_commune_id_is_wrong() {
        // given
        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setPhone("0975944854");
        request.setCommuneId("abcde");
        request.setFullName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_COMMUNE, exception.getMessage());
    }

    @Test
    public void test_create_customer_address_fail_when_phone_is_abc() {
        // given
        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setPhone("abc");
        request.setCommuneId("00006");
        request.setFullName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_create_customer_address_fail_when_phone_is_empty() {
        // given
        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setPhone("");
        request.setCommuneId("00006");
        request.setFullName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_create_customer_address_fail_when_phone_length_is_longer_than_expect() {
        // given
        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setPhone("08653900311");
        request.setCommuneId("00006");
        request.setFullName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_create_customer_address_fail_when_phone_is_null() {
        // given
        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setPhone(null);
        request.setCommuneId("00006");
        request.setFullName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_create_customer_address_fail_when_phone_length_is_shorter_than_expect() {
        // given
        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setPhone("08653900");
        request.setCommuneId("00006");
        request.setFullName("Nguyễn Thị Hồng Nhung");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_create_customer_address_success_when_full_name_does_not_trim() {
        // given
        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setPhone("0975944854");
        request.setCommuneId("00006");
        request.setFullName(" Nhung Nguyễn    ");

        // when
        setUserContext(36L, "0865390037");
        CreateAddressResponse response = underTest.createCustomerAddress(request).getBody();

        // then
        log.info("Address id: " + response.getAddressId());
        Assertions.assertEquals(CREATE_ADDRESS_SUCCESS, response.getMessage());
    }

    @Test
    public void test_create_customer_address_fail_when_full_name_is_empty() {
        // given
        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setPhone("0865390031");
        request.setCommuneId("00006");
        request.setFullName("");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_FULL_NAME, exception.getMessage());
    }

    @Test
    public void test_create_customer_address_fail_when_full_name_is_null() {
        // given
        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setPhone("0865390031");
        request.setCommuneId("00006");
        request.setFullName(null);

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_FULL_NAME, exception.getMessage());
    }

    @Test
    public void test_create_customer_address_fail_when_full_name_contain_special_character() {
        // given
        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreetAddress("Đường 30m Hòa Lạc");
        request.setPhone("0865390031");
        request.setCommuneId("00006");
        request.setFullName("Nhung @123");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createCustomerAddress(request));

        // then
        Assertions.assertEquals(INVALID_FULL_NAME, exception.getMessage());
    }

    @Test
    public void test_getCustomerProfile_success() {
        // given
        CustomerProfileRequest request = new CustomerProfileRequest();

        // when
        setUserContext(36L, "0865390037");
        CustomerProfileResponse response = underTest.getCustomerProfile(request).getBody();

        // then
        Assertions.assertEquals("https://storage.googleapis.com/download/storage/v1/b/flix_public/" +
                        "o/4492f2aa-fd13-4190-b75b-62d4173384e9e43756fc-8a23-4b46-9454-a4e5cd1eea52.jpg?generation=1655141000031817&alt=media",
                response.getAvatarUrl());
        Assertions.assertEquals("0865390037", response.getPhone());
        Assertions.assertEquals("thang@gmail.com", response.getEmail());
        Assertions.assertEquals("08-03-2000", response.getDateOfBirth());
        Assertions.assertEquals(false, response.getGender());
        Assertions.assertEquals("Faker", response.getFullName());
    }

    @Test
    public void test_update_customer_profile_success_when_gender_is_false() {
        // given
        UpdateCustomerProfileRequest request = new UpdateCustomerProfileRequest();
        request.setFullName("Nguyễn Thị Hồng Nhung");
        request.setDateOfBirth("08-03-2000");
        request.setGender(false);
        request.setEmail("nhungnthhe141425@fpt.edu.vn");

        // when
        setUserContext(36L, "0865390037");
        UpdateCustomerProfileResponse response = underTest.updateCustomerProfile(request).getBody();
        User user = validatorService.getUserValidated(request.getUsername());

        // then
        Assertions.assertEquals(user.getFullName(), "Nguyễn Thị Hồng Nhung");
        Assertions.assertEquals(DateFormatUtil.toString(user.getDateOfBirth(), DATE_PATTERN), "08-03-2000");
        Assertions.assertEquals(user.getGender(), false);
        Assertions.assertEquals(user.getEmail(), "nhungnthhe141425@fpt.edu.vn");
        Assertions.assertEquals(UPDATED_PROFILE_SUCCESS, response.getMessage());
    }

    @Test
    public void test_update_customer_profile_fail_when_email_is_empty() {
        // given
        UpdateCustomerProfileRequest request = new UpdateCustomerProfileRequest();
        request.setFullName("Nguyễn Thị Hồng Nhung");
        request.setDateOfBirth("08-03-2000");
        request.setGender(false);
        request.setEmail("");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.updateCustomerProfile(request));

        // then
        Assertions.assertEquals(INVALID_EMAIL, exception.getMessage());
    }

    @Test
    public void test_update_customer_profile_fail_when_dob_is_after_today() {
        // given
        UpdateCustomerProfileRequest request = new UpdateCustomerProfileRequest();
        String dob = DateFormatUtil.toString(LocalDate.now().plusDays(1), DATE_PATTERN);
        request.setFullName("Nguyễn Thị Hồng Nhung");
        request.setDateOfBirth(dob);
        request.setGender(false);
        request.setEmail("nhungnthhe141425@fpt.edu.vn");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.updateCustomerProfile(request));

        // then
        Assertions.assertEquals(DOB_MUST_BE_LESS_THAN_OR_EQUAL_TODAY, exception.getMessage());
    }

    @Test
    public void test_update_customer_profile_success_when_email_is_null() {
        // given
        UpdateCustomerProfileRequest request = new UpdateCustomerProfileRequest();
        request.setFullName("Nguyễn Thị Hồng Nhung");
        request.setDateOfBirth("08-03-2000");
        request.setGender(false);
        request.setEmail(null);

        // when
        setUserContext(36L, "0865390037");
        UpdateCustomerProfileResponse response = underTest.updateCustomerProfile(request).getBody();
        User user = validatorService.getUserValidated(request.getUsername());

        // then
        Assertions.assertEquals(user.getFullName(), "Nguyễn Thị Hồng Nhung");
        Assertions.assertEquals(DateFormatUtil.toString(user.getDateOfBirth(), DATE_PATTERN), "08-03-2000");
        Assertions.assertEquals(user.getGender(), false);
        Assertions.assertNull(user.getEmail());
        Assertions.assertEquals(UPDATED_PROFILE_SUCCESS, response.getMessage());
    }

    @Test
    public void test_update_customer_profile_fail_when_email_no_extension() {
        // given
        UpdateCustomerProfileRequest request = new UpdateCustomerProfileRequest();
        request.setFullName("Nguyễn Thị Hồng Nhung");
        request.setDateOfBirth("08-03-2000");
        request.setGender(false);
        request.setEmail("nhungnthgmail.com");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.updateCustomerProfile(request));

        // then
        Assertions.assertEquals(INVALID_EMAIL, exception.getMessage());
    }

    @Test
    public void test_update_customer_profile_fail_when_email_is_123() {
        // given
        UpdateCustomerProfileRequest request = new UpdateCustomerProfileRequest();
        request.setFullName("Nguyễn Thị Hồng Nhung");
        request.setDateOfBirth("08-03-2000");
        request.setGender(false);
        request.setEmail("123");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.updateCustomerProfile(request));

        // then
        Assertions.assertEquals(INVALID_EMAIL, exception.getMessage());
    }

    @Test
    public void test_update_customer_profile_success_when_gender_is_true() {
        // given
        UpdateCustomerProfileRequest request = new UpdateCustomerProfileRequest();
        request.setFullName("Nguyễn Thị Hồng Nhung");
        request.setDateOfBirth("08-03-2000");
        request.setGender(true);
        request.setEmail("nhungnthhe141425@fpt.edu.vn");

        // when
        setUserContext(36L, "0865390037");
        UpdateCustomerProfileResponse response = underTest.updateCustomerProfile(request).getBody();
        User user = validatorService.getUserValidated(request.getUsername());

        // then
        Assertions.assertEquals(user.getFullName(), "Nguyễn Thị Hồng Nhung");
        Assertions.assertEquals(DateFormatUtil.toString(user.getDateOfBirth(), DATE_PATTERN), "08-03-2000");
        Assertions.assertEquals(user.getGender(), true);
        Assertions.assertEquals(user.getEmail(), "nhungnthhe141425@fpt.edu.vn");
        Assertions.assertEquals(UPDATED_PROFILE_SUCCESS, response.getMessage());
    }

    @Test
    public void test_update_customer_profile_success_when_gender_is_null() {
        // given
        UpdateCustomerProfileRequest request = new UpdateCustomerProfileRequest();
        request.setFullName("Nguyễn Thị Hồng Nhung");
        request.setDateOfBirth("08-03-2000");
        request.setGender(null);
        request.setEmail("nhungnthhe141425@fpt.edu.vn");

        // when
        setUserContext(36L, "0865390037");
        UpdateCustomerProfileResponse response = underTest.updateCustomerProfile(request).getBody();
        User user = validatorService.getUserValidated(request.getUsername());

        // then
        Assertions.assertEquals(user.getFullName(), "Nguyễn Thị Hồng Nhung");
        Assertions.assertEquals(DateFormatUtil.toString(user.getDateOfBirth(), DATE_PATTERN), "08-03-2000");
        Assertions.assertNull(user.getGender());
        Assertions.assertEquals(user.getEmail(), "nhungnthhe141425@fpt.edu.vn");
        Assertions.assertEquals(UPDATED_PROFILE_SUCCESS, response.getMessage());
    }

    @Test
    public void test_update_customer_profile_fail_when_DBO_is_wrong_format() {
        // given
        UpdateCustomerProfileRequest request = new UpdateCustomerProfileRequest();
        request.setFullName("Nguyễn Thị Hồng Nhung");
        request.setDateOfBirth("20/05/2000");
        request.setGender(true);
        request.setEmail("nhungnthhe141425@fpt.edu.vn");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.updateCustomerProfile(request));

        // then
        Assertions.assertEquals(WRONG_LOCAL_DATE_FORMAT, exception.getMessage());
    }

    @Test
    public void test_update_customer_profile_fail_when_DBO_is_empty() {
        // given
        UpdateCustomerProfileRequest request = new UpdateCustomerProfileRequest();
        request.setFullName("Nguyễn Thị Hồng Nhung");
        request.setDateOfBirth("");
        request.setGender(true);
        request.setEmail("nhungnthhe141425@fpt.edu.vn");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.updateCustomerProfile(request));

        // then
        Assertions.assertEquals(WRONG_LOCAL_DATE_FORMAT, exception.getMessage());
    }

    @Test
    public void test_update_customer_profile_success_when_DBO_is_null() {
        // given
        UpdateCustomerProfileRequest request = new UpdateCustomerProfileRequest();
        request.setFullName("Nguyễn Thị Hồng Nhung");
        request.setDateOfBirth(null);
        request.setGender(true);
        request.setEmail("nhungnthhe141425@fpt.edu.vn");

        // when
        setUserContext(36L, "0865390037");
        UpdateCustomerProfileResponse response = underTest.updateCustomerProfile(request).getBody();
        User user = validatorService.getUserValidated(request.getUsername());

        // then
        Assertions.assertEquals(user.getFullName(), "Nguyễn Thị Hồng Nhung");
        Assertions.assertNull(user.getDateOfBirth());
        Assertions.assertEquals(true, user.getGender());
        Assertions.assertEquals(user.getEmail(), "nhungnthhe141425@fpt.edu.vn");
        Assertions.assertEquals(UPDATED_PROFILE_SUCCESS, response.getMessage());
    }

    @Test
    public void test_update_customer_profile_success_when_full_name_does_not_trim() {
        // given
        UpdateCustomerProfileRequest request = new UpdateCustomerProfileRequest();
        request.setFullName(" Nhung Nguyễn    ");
        request.setDateOfBirth("08-03-2000");
        request.setGender(true);
        request.setEmail("nhungnthhe141425@fpt.edu.vn");

        // when
        setUserContext(36L, "0865390037");
        UpdateCustomerProfileResponse response = underTest.updateCustomerProfile(request).getBody();
        User user = validatorService.getUserValidated(request.getUsername());

        // then
        Assertions.assertEquals(user.getFullName(), " Nhung Nguyễn    ");
        Assertions.assertEquals(DateFormatUtil.toString(user.getDateOfBirth(), DATE_PATTERN), "08-03-2000");
        Assertions.assertEquals(true, user.getGender());
        Assertions.assertEquals(user.getEmail(), "nhungnthhe141425@fpt.edu.vn");
        Assertions.assertEquals(UPDATED_PROFILE_SUCCESS, response.getMessage());
    }

    @Test
    public void test_update_customer_profile_fail_when_full_name_is_empty() {
        // given
        UpdateCustomerProfileRequest request = new UpdateCustomerProfileRequest();
        request.setFullName("");
        request.setDateOfBirth("20-05-2000");
        request.setGender(true);
        request.setEmail("nhungnthhe141425@fpt.edu.vn");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.updateCustomerProfile(request));

        // then
        Assertions.assertEquals(INVALID_FULL_NAME, exception.getMessage());
    }

    @Test
    public void test_update_customer_profile_fail_when_full_name_is_null() {
        // given
        UpdateCustomerProfileRequest request = new UpdateCustomerProfileRequest();
        request.setFullName(null);
        request.setDateOfBirth("20-05-2000");
        request.setGender(true);
        request.setEmail("nhungnthhe141425@fpt.edu.vn");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.updateCustomerProfile(request));

        // then
        Assertions.assertEquals(INVALID_FULL_NAME, exception.getMessage());
    }

    @Test
    public void test_update_customer_profile_fail_when_full_name_contain_special_character() {
        // given
        UpdateCustomerProfileRequest request = new UpdateCustomerProfileRequest();
        request.setFullName("Nhung @123");
        request.setDateOfBirth("20-05-2000");
        request.setGender(true);
        request.setEmail("nhungnthhe141425@fpt.edu.vn");

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.updateCustomerProfile(request));

        // then
        Assertions.assertEquals(INVALID_FULL_NAME, exception.getMessage());
    }

    @Test
    public void test_get_repairer_profile_success() {
        // given
        RepairerRequest request = new RepairerRequest();
        request.setRepairerId(52L);

        // when
        setUserContext(36L, "0865390037");
        RepairerResponse response = underTest.getRepairerProfile(request).getBody();

        // then
        Assertions.assertNotNull(response);
    }

    @Test
    public void test_get_repairer_profile_return_null_field_when_repairer_id_is_null() {
        // given
        RepairerRequest request = new RepairerRequest();
        request.setRepairerId(null);

        // when
        setUserContext(36L, "0865390037");
        RepairerResponse response = underTest.getRepairerProfile(request).getBody();

        // then
        Assertions.assertNull(response.getExperienceDescription());
        Assertions.assertNull(response.getRepairerName());
        Assertions.assertNull(response.getJointAt());
        Assertions.assertNull(response.getExperienceYear());
        Assertions.assertNull(response.getSuccessfulRepair());
        Assertions.assertNull(response.getRating());
    }

    @Test
    public void test_get_repairer_profile_return_null_field_when_repairer_id_is_0() {
        // given
        RepairerRequest request = new RepairerRequest();
        request.setRepairerId(0L);

        // when
        setUserContext(36L, "0865390037");
        RepairerResponse response = underTest.getRepairerProfile(request).getBody();

        // then
        Assertions.assertNull(response.getExperienceDescription());
        Assertions.assertNull(response.getRepairerName());
        Assertions.assertNull(response.getJointAt());
        Assertions.assertNull(response.getExperienceYear());
        Assertions.assertEquals(0L, response.getSuccessfulRepair());
        Assertions.assertNull(response.getRating());
    }

    @Test
    public void test_get_repairer_profile_return_null_field_when_repairer_id_is_negative_number() {
        // given
        RepairerRequest request = new RepairerRequest();
        request.setRepairerId(-1L);

        // when
        setUserContext(36L, "0865390037");
        RepairerResponse response = underTest.getRepairerProfile(request).getBody();

        // then
        Assertions.assertNull(response.getExperienceDescription());
        Assertions.assertNull(response.getRepairerName());
        Assertions.assertNull(response.getJointAt());
        Assertions.assertNull(response.getExperienceYear());
        Assertions.assertEquals(0L, response.getSuccessfulRepair());
        Assertions.assertNull(response.getRating());
    }

    @Test
    public void test_get_repairer_comments_success_when_offset_and_limit_are_null() {
        // given
        RepairerCommentRequest request = new RepairerCommentRequest();
        request.setRepairerId(52L);
        request.setLimit(null);
        request.setOffset(null);

        // when
        setUserContext(36L, "0865390037");
        RepairerCommentResponse response = underTest.getRepairerComments(request).getBody();

        // then
        Assertions.assertNotNull(response.getRepairerComments());
    }

    @Test
    public void test_get_repairer_comments_fail_when_repairer_id_is_null() {
        // given
        RepairerCommentRequest request = new RepairerCommentRequest();
        request.setRepairerId(null);
        request.setLimit(null);
        request.setOffset(null);

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getRepairerComments(request));

        // then
        Assertions.assertEquals(REPAIRER_ID_IS_REQUIRED, exception.getMessage());
    }

    @Test
    public void test_get_repairer_comments_success_when_repairer_id_is_0() {
        // given
        RepairerCommentRequest request = new RepairerCommentRequest();
        request.setRepairerId(0L);
        request.setLimit(null);
        request.setOffset(null);

        // when
        setUserContext(36L, "0865390037");
        RepairerCommentResponse response = underTest.getRepairerComments(request).getBody();

        // then
        Assertions.assertEquals(0, response.getRepairerComments().size());
    }

    @Test
    public void test_get_repairer_comments_success_when_repairer_id_is_negative() {
        // given
        RepairerCommentRequest request = new RepairerCommentRequest();
        request.setRepairerId(-1L);
        request.setLimit(null);
        request.setOffset(null);

        // when
        setUserContext(36L, "0865390037");
        RepairerCommentResponse response = underTest.getRepairerComments(request).getBody();

        // then
        Assertions.assertEquals(0, response.getRepairerComments().size());
    }

    @Test
    public void test_get_repairer_comments_success_when_limit_is_10_offset_is_0() {
        // given
        RepairerCommentRequest request = new RepairerCommentRequest();
        request.setRepairerId(52L);
        request.setLimit(10);
        request.setOffset(0);

        // when
        setUserContext(36L, "0865390037");
        RepairerCommentResponse response = underTest.getRepairerComments(request).getBody();

        // then
        Assertions.assertNotNull(response.getRepairerComments());
    }

    @Test
    public void test_get_repairer_comments_fail_when_limit_is_negative() {
        // given
        RepairerCommentRequest request = new RepairerCommentRequest();
        request.setRepairerId(52L);
        request.setLimit(-1);
        request.setOffset(0);

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getRepairerComments(request));

        // then
        Assertions.assertEquals(LIMIT_MUST_BE_GREATER_OR_EQUAL_0, exception.getMessage());
    }

    @Test
    public void test_get_repairer_comments_success_when_limit_is_10_offset_is_2_repairer_id_is_52() {
        // given
        RepairerCommentRequest request = new RepairerCommentRequest();
        request.setRepairerId(52L);
        request.setLimit(10);
        request.setOffset(2);

        // when
        setUserContext(36L, "0865390037");
        RepairerCommentResponse response = underTest.getRepairerComments(request).getBody();

        // then
        Assertions.assertNotNull(response.getRepairerComments());
    }

    @Test
    public void test_get_repairer_comments_fail_when_offst_is_negative() {
        // given
        RepairerCommentRequest request = new RepairerCommentRequest();
        request.setRepairerId(52L);
        request.setLimit(10);
        request.setOffset(-1);

        // when
        setUserContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getRepairerComments(request));

        // then
        Assertions.assertEquals(OFFSET_MUST_BE_GREATER_OR_EQUAL_0, exception.getMessage());
    }

    @Test
    public void test_get_repairer_comments_success_when_limit_is_0_offset_is_0_repairer_id_is_52() {
        // given
        RepairerCommentRequest request = new RepairerCommentRequest();
        request.setRepairerId(52L);
        request.setLimit(0);
        request.setOffset(0);

        // when
        setUserContext(36L, "0865390037");
        RepairerCommentResponse response = underTest.getRepairerComments(request).getBody();

        // then
        Assertions.assertNotNull(response.getRepairerComments());
    }

    @Test
    public void test_choose_main_address_success() {
        // given
        long userId = 36L;
        long addressId = 21L;
        ChooseMainAddressRequest request = new ChooseMainAddressRequest();
        request.setAddressId(addressId);


        // when
        setUserContext(userId, "0865390037");
        ChooseMainAddressResponse response = underTest.chooseMainAddress(request).getBody();
        UserAddress userAddress = userAddressDAO.findByUserIdAndIsMainAddressAndDeletedAtIsNull(userId, true).get();

        // then
        Assertions.assertEquals(CHOOSING_MAIN_ADDRESS_SUCCESS, response.getMessage());
        Assertions.assertEquals(addressId, userAddress.getId());
    }

    @Test
    public void test_choose_main_address_fail_when_address_id_is_null() {
        // given
        long userId = 36L;
        ChooseMainAddressRequest request = new ChooseMainAddressRequest();
        request.setAddressId(null);

        // when
        setUserContext(userId, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.chooseMainAddress(request).getBody());

        // then
        Assertions.assertEquals(ADDRESS_ID_IS_REQUIRED, exception.getMessage());
    }

    void setUserContext(Long id, String phone) {
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