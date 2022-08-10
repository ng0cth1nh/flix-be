package com.fu.flix.service.impl;

import com.fu.flix.dao.CommentDAO;
import com.fu.flix.dto.ExtraServiceInputDTO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.dto.security.UserPrincipal;
import com.fu.flix.service.ConfirmedUserService;
import com.fu.flix.service.CustomerService;
import com.fu.flix.service.RepairerService;
import com.fu.flix.util.DateFormatUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.fu.flix.constant.Constant.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class ConfirmedUserServiceImplTest {
    @Autowired
    ConfirmedUserService underTest;
    @Autowired
    CommentDAO commentDAO;
    String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    @Autowired
    RepairerService repairerService;
    @Autowired
    CustomerService customerService;

    @Test
    public void test_comment_success_with_rating_is_5() {
        // given
        String requestCode = "0908224D3DCA";
        Integer rating = 5;
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(rating);
        request.setComment(comment);
        setCustomerContext(40L, "0962706248");

        // when
        ResponseEntity<CommentResponse> responseEntity = underTest.createComment(request);
        CommentResponse response = responseEntity.getBody();

        // then
        Assertions.assertEquals(COMMENT_SUCCESS, response.getMessage());
    }

    @Test
    public void test_comment_success_with_rating_is_1() {
        // given
        String requestCode = "0908224D3DCA";
        Integer rating = 1;
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(rating);
        request.setComment(comment);
        setCustomerContext(40L, "0962706248");

        // when
        ResponseEntity<CommentResponse> responseEntity = underTest.createComment(request);
        CommentResponse response = responseEntity.getBody();

        // then
        Assertions.assertEquals(COMMENT_SUCCESS, response.getMessage());
    }

    @Test
    public void test_comment_success_with_rating_is_4() {
        // given
        String requestCode = "0908224D3DCA";
        Integer rating = 4;
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(rating);
        request.setComment(comment);
        setCustomerContext(40L, "0962706248");

        // when
        ResponseEntity<CommentResponse> responseEntity = underTest.createComment(request);
        CommentResponse response = responseEntity.getBody();

        // then
        Assertions.assertEquals(COMMENT_SUCCESS, response.getMessage());
    }

    @Test
    public void test_comment_fail_when_rating_is_6() {
        // given
        String requestCode = "0908224D3DCA";
        Integer rating = 6;
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(rating);
        request.setComment(comment);
        setCustomerContext(40L, "0962706248");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createComment(request));

        // then
        Assertions.assertEquals(RATING_MUST_IN_RANGE_1_TO_5, exception.getMessage());
    }

    @Test
    public void test_comment_fail_when_rating_is_0() {
        // given
        String requestCode = "0908224D3DCA";
        Integer rating = 0;
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(rating);
        request.setComment(comment);
        setCustomerContext(40L, "0962706248");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createComment(request));

        // then
        Assertions.assertEquals(RATING_MUST_IN_RANGE_1_TO_5, exception.getMessage());
    }

    @Test
    public void test_comment_fail_when_rating_is_null() {
        // given
        String requestCode = "0908224D3DCA";
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(null);
        request.setComment(comment);
        setCustomerContext(40L, "0962706248");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createComment(request));

        // then
        Assertions.assertEquals(RATING_IS_REQUIRED, exception.getMessage());
    }

    @Test
    public void test_comment_success_when_comment_is_null() {
        // given
        String requestCode = "0908224D3DCA";
        Integer rating = 4;
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(rating);
        request.setComment(null);
        setCustomerContext(40L, "0962706248");

        // when
        ResponseEntity<CommentResponse> responseEntity = underTest.createComment(request);
        CommentResponse response = responseEntity.getBody();

        // then
        Assertions.assertEquals(COMMENT_SUCCESS, response.getMessage());
    }

    @Test
    public void test_comment_success_when_comment_is_empty() {
        // given
        String requestCode = "0908224D3DCA";
        Integer rating = 4;
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(rating);
        request.setComment("");
        setCustomerContext(40L, "0962706248");

        // when
        ResponseEntity<CommentResponse> responseEntity = underTest.createComment(request);
        CommentResponse response = responseEntity.getBody();

        // then
        Assertions.assertEquals(COMMENT_SUCCESS, response.getMessage());
    }

    @Test
    public void test_comment_fail_when_comment_length_is_2501() {
        // given
        String requestCode = "030722WGR4WV";
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(null);
        request.setComment("t".repeat(2501));
        setCustomerContext(48L, "0962706248");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createComment(request));

        // then
        Assertions.assertEquals(EXCEEDED_COMMENT_LENGTH_ALLOWED, exception.getMessage());
    }

    @Test
    public void test_comment_fail_when_invalid_request_code() {
        // given
        String requestCode = "34AEFEMWQGNN";
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(4);
        request.setComment(comment);
        setCustomerContext(48L, "0962706248");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createComment(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_comment_fail_when_request_code_is_empty() {
        // given
        String requestCode = "";
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(4);
        request.setComment(comment);
        setCustomerContext(48L, "0962706248");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createComment(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_comment_fail_when_request_code_is_null() {
        // given
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode(null);
        request.setRating(4);
        request.setComment(comment);
        setCustomerContext(48L, "0962706248");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createComment(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_comment_fail_when_request_code_and_user_does_not_match() {
        // given
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode("0908224D3DCA");
        request.setRating(4);
        request.setComment(comment);
        setCustomerContext(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createComment(request));

        // then
        Assertions.assertEquals(USER_AND_REQUEST_CODE_DOES_NOT_MATCH, exception.getMessage());
    }

    @Test
    public void test_comment_fail_when_comment_existed() {
        // given
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode("0908222283BF");
        request.setRating(4);
        request.setComment(comment);
        setCustomerContext(40L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createComment(request));

        // then
        Assertions.assertEquals(COMMENT_EXISTED, exception.getMessage());
    }

    @Test
    public void test_comment_fail_when_request_status_is_not_done() throws IOException {
        // given
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        String requestCode = createFixingRequestForCustomerId40();
        request.setRequestCode(requestCode);
        request.setRating(4);
        request.setComment(comment);
        setCustomerContext(40L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createComment(request));

        // then
        Assertions.assertEquals(CAN_NOT_COMMENT_WHEN_STATUS_NOT_DONE, exception.getMessage());
    }

    @Test
    public void test_get_invoice_success_for_repairer() throws IOException {
        // given
        String requestCode = createFixingRequestFoCustomerId36();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);
        createInvoiceByRepairerId56(requestCode);

        GetInvoiceRequest request = new GetInvoiceRequest();
        request.setRequestCode(requestCode);

        setRepairerContext(56L, "0865390057");

        // when
        GetInvoiceResponse response = underTest.getInvoice(request).getBody();

        // then
        Assertions.assertNotNull(response);
    }

    @Test
    public void test_get_invoice_success_for_customer() throws IOException {
        // given
        String requestCode = createFixingRequestFoCustomerId36();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);
        createInvoiceByRepairerId56(requestCode);

        GetInvoiceRequest request = new GetInvoiceRequest();
        request.setRequestCode(requestCode);

        setCustomerContext(36L, "0865390057");

        // when
        GetInvoiceResponse response = underTest.getInvoice(request).getBody();

        // then
        Assertions.assertNotNull(response);
    }

    @Test
    public void test_get_invoice_fail_when_request_code_is_null() {
        // given
        GetInvoiceRequest request = new GetInvoiceRequest();
        request.setRequestCode(null);

        setCustomerContext(36L, "0865390057");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getInvoice(request).getBody());

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_get_fixed_services_success_by_repairer() throws IOException {
        // given
        String requestCode = createFixingRequestFoCustomerId36();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);
        putExtraServiceToInvoiceByRepairerId56(requestCode);
        putSubServicesToInvoiceByRepairerId56(requestCode);
        putAccessoriesToInvoiceByRepairerId56(requestCode);
        createInvoiceByRepairerId56(requestCode);

        GetFixedServiceRequest request = new GetFixedServiceRequest();
        request.setRequestCode(requestCode);

        setRepairerContext(56L, "0865390052");

        // when
        GetFixedServiceResponse response = underTest.getFixedServices(request).getBody();

        // then
        Assertions.assertNotNull(response);
    }

    @Test
    public void test_get_fixed_services_fail_when_status_is_not_valid() throws IOException {
        // given
        String requestCode = createFixingRequestFoCustomerId36();
        approvalRequestByRepairerId56(requestCode);

        GetFixedServiceRequest request = new GetFixedServiceRequest();
        request.setRequestCode(requestCode);

        setRepairerContext(56L, "0865390052");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getFixedServices(request));

        // then
        Assertions.assertEquals(JUST_CAN_GET_FIXED_SERVICES_WHEN_REQUEST_STATUS_IS_PAYMENT_WAITING_OR_DONE_OR_FIXING, exception.getMessage());
    }

    @Test
    public void test_get_fixed_services_fail_when_customer_does_not_have_permission() throws IOException {
        // given
        String requestCode = createFixingRequestFoCustomerId36();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);
        putExtraServiceToInvoiceByRepairerId56(requestCode);
        putSubServicesToInvoiceByRepairerId56(requestCode);
        putAccessoriesToInvoiceByRepairerId56(requestCode);
        createInvoiceByRepairerId56(requestCode);

        GetFixedServiceRequest request = new GetFixedServiceRequest();
        request.setRequestCode(requestCode);

        setCustomerContext(48L, "0865390052");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getFixedServices(request));

        // then
        Assertions.assertEquals(CUSTOMER_DOES_NOT_HAVE_PERMISSION_TO_GET_FIXED_SERVICES_FOR_THIS_INVOICE, exception.getMessage());
    }

    @Test
    public void test_get_fixed_services_fail_when_repairer_does_not_have_permission() throws IOException {
        // given
        String requestCode = createFixingRequestFoCustomerId36();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);
        putExtraServiceToInvoiceByRepairerId56(requestCode);
        putSubServicesToInvoiceByRepairerId56(requestCode);
        putAccessoriesToInvoiceByRepairerId56(requestCode);
        createInvoiceByRepairerId56(requestCode);

        GetFixedServiceRequest request = new GetFixedServiceRequest();
        request.setRequestCode(requestCode);

        setRepairerContext(52L, "0865390052");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getFixedServices(request));

        // then
        Assertions.assertEquals(REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_GET_FIXED_SERVICES_FOR_THIS_INVOICE, exception.getMessage());
    }

    @Test
    public void test_get_fixed_services_success_by_customer() throws IOException {
        // given
        String requestCode = createFixingRequestFoCustomerId36();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);
        putExtraServiceToInvoiceByRepairerId56(requestCode);
        putSubServicesToInvoiceByRepairerId56(requestCode);
        putAccessoriesToInvoiceByRepairerId56(requestCode);
        createInvoiceByRepairerId56(requestCode);

        GetFixedServiceRequest request = new GetFixedServiceRequest();
        request.setRequestCode(requestCode);

        setCustomerContext(36L, "0865390052");

        // when
        GetFixedServiceResponse response = underTest.getFixedServices(request).getBody();

        // then
        Assertions.assertNotNull(response);
    }

    private void putExtraServiceToInvoiceByRepairerId56(String requestCode) {
        List<ExtraServiceInputDTO> extraServices = new ArrayList<>();
        extraServices.add(new ExtraServiceInputDTO("Tiền lau WC", null, 25000L, null));
        extraServices.add(new ExtraServiceInputDTO("Tiền thổi kèn", "meo meo", 25000L, 3));

        AddExtraServiceToInvoiceRequest request = new AddExtraServiceToInvoiceRequest();
        request.setRequestCode(requestCode);
        request.setExtraServices(extraServices);

        setRepairerContext(56L, "0865390056");
        repairerService.putExtraServicesToInvoice(request);
    }

    private void putSubServicesToInvoiceByRepairerId56(String requestCode) {
        List<Long> serviceIds = new ArrayList<>();
        serviceIds.add(1L);
        serviceIds.add(2L);

        AddSubServicesToInvoiceRequest request = new AddSubServicesToInvoiceRequest();
        request.setRequestCode(requestCode);
        request.setSubServiceIds(serviceIds);

        setRepairerContext(56L, "0865390056");
        repairerService.putSubServicesToInvoice(request);
    }

    private void putAccessoriesToInvoiceByRepairerId56(String requestCode) {
        List<Long> accessoryIds = new ArrayList<>();
        accessoryIds.add(1L);
        accessoryIds.add(2L);

        AddAccessoriesToInvoiceRequest request = new AddAccessoriesToInvoiceRequest();
        request.setRequestCode(requestCode);
        request.setAccessoryIds(accessoryIds);

        setRepairerContext(56L, "0865390056");
        repairerService.putAccessoriesToInvoice(request).getBody();
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

    private String createFixingRequestFoCustomerId36() throws IOException {
        setCustomerContext(36L, "0865390037");
        Long serviceId = 1L;
        Long addressId = 7L;
        String expectFixingDay = DateFormatUtil.toString(LocalDateTime.now().plusDays(2L), DATE_TIME_PATTERN);
        String description = "Thợ phải đẹp trai nha";
        String paymentMethodId = "C";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setDescription(description);
        request.setExpectFixingDay(expectFixingDay);
        request.setAddressId(addressId);
        request.setPaymentMethodId(paymentMethodId);

        RequestingRepairResponse response = customerService.createFixingRequest(request).getBody();
        return response.getRequestCode();
    }

    private String createFixingRequestForCustomerId40() throws IOException {
        setCustomerContext(40L, "0865390037");
        Long serviceId = 1L;
        Long addressId = 11L;
        String expectFixingDay = DateFormatUtil.toString(LocalDateTime.now().plusDays(2L), DATE_TIME_PATTERN);
        String description = "Thợ phải đẹp trai nha";
        String paymentMethodId = "C";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setDescription(description);
        request.setExpectFixingDay(expectFixingDay);
        request.setAddressId(addressId);
        request.setPaymentMethodId(paymentMethodId);

        RequestingRepairResponse response = customerService.createFixingRequest(request).getBody();
        return response.getRequestCode();
    }

    private void approvalRequestByRepairerId56(String requestCode) throws IOException {
        setRepairerContext(56L, "0865390056");
        RepairerApproveRequest request = new RepairerApproveRequest();
        request.setRequestCode(requestCode);
        repairerService.approveRequest(request);
    }

    private void confirmFixingByRepairerId56(String requestCode) throws IOException {
        ConfirmFixingRequest request = new ConfirmFixingRequest();
        request.setRequestCode(requestCode);
        setRepairerContext(56L, "0865390037");
        repairerService.confirmFixing(request);
    }

    private void createInvoiceByRepairerId56(String requestCode) throws IOException {
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setRequestCode(requestCode);
        setRepairerContext(56L, "0865390037");
        repairerService.createInvoice(request);
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