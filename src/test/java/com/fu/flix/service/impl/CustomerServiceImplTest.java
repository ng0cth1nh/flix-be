package com.fu.flix.service.impl;

import com.fu.flix.dao.CommentDAO;
import com.fu.flix.dao.RepairRequestDAO;
import com.fu.flix.dao.UserAddressDAO;
import com.fu.flix.dto.UserAddressDTO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.dto.security.UserPrincipal;
import com.fu.flix.service.CustomerService;
import com.fu.flix.service.RepairerService;
import com.fu.flix.service.ValidatorService;
import com.fu.flix.util.DateFormatUtil;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.fu.flix.constant.Constant.*;
import static com.fu.flix.constant.enums.RequestStatus.PENDING;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
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
        Assertions.assertTrue(response.getRequestHistories().size() > 0);
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
        Assertions.assertNull(response.getPrice());
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
        RequestingDetailForCustomerResponse response = underTest.getDetailFixingRequest(request).getBody();

        // then
        Assertions.assertNull(response.getServiceName());
        Assertions.assertNull(response.getActualPrice());
        Assertions.assertNull(response.getCustomerName());
        Assertions.assertNull(response.getRequestCode());
        Assertions.assertNull(response.getPrice());
        Assertions.assertNull(response.getDate());
    }

    @Test
    public void test_getDetailFixingRequest_fail_when_request_code_is_null() {
        // given
        RequestingDetailForCustomerRequest request = new RequestingDetailForCustomerRequest();
        request.setRequestCode(null);

        // when
        setUserContext(36L, "0865390037");
        RequestingDetailForCustomerResponse response = underTest.getDetailFixingRequest(request).getBody();

        // then
        Assertions.assertNull(response.getServiceName());
        Assertions.assertNull(response.getActualPrice());
        Assertions.assertNull(response.getCustomerName());
        Assertions.assertNull(response.getRequestCode());
        Assertions.assertNull(response.getPrice());
        Assertions.assertNull(response.getDate());
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

    void setUserContext(Long id, String phone) {
        String[] roles = {"ROLE_CUSTOMER"};
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(new UserPrincipal(id, phone), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

}