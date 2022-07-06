package com.fu.flix.service.impl;

import com.fu.flix.dao.CommentDAO;
import com.fu.flix.dao.RepairRequestDAO;
import com.fu.flix.dao.UserAddressDAO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.RequestingRepairRequest;
import com.fu.flix.dto.response.RequestingRepairResponse;
import com.fu.flix.dto.security.UserPrincipal;
import com.fu.flix.service.CustomerService;
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

        setContextUsername(36L, "0865390037");

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

        setContextUsername(36L, "0865390037");

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

        setContextUsername(36L, "0865390037");

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

        setContextUsername(36L, "0865390037");

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

        setContextUsername(36L, "0865390037");

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

        setContextUsername(36L, "0865390037");

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

        setContextUsername(36L, "0865390037");

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

        setContextUsername(36L, "0865390037");

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

        setContextUsername(36L, "0865390037");

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

        setContextUsername(36L, "0865390037");

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

        setContextUsername(36L, "0865390037");

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

        setContextUsername(36L, "0865390037");

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

        setContextUsername(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFixingRequest(request));

        // then
        Assertions.assertEquals(INVALID_SERVICE, exception.getMessage());
    }

    @Test
    public void test_create_fixing_request_fail_when_fixing_day_is_more_than_today_30_days() {
        // given
        Long serviceId = 1L;
        Long addressId = 0L;
        String expectFixingDay = DateFormatUtil.toString(LocalDateTime.now().plusDays(30L).plusSeconds(1L), DATE_TIME_PATTERN);
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

        setContextUsername(36L, "0865390037");

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

        setContextUsername(36L, "0865390037");

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

        setContextUsername(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFixingRequest(request));

        // then
        Assertions.assertEquals(EXCEEDED_DESCRIPTION_LENGTH_ALLOWED, exception.getMessage());
    }

    void setContextUsername(Long id, String phone) {
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