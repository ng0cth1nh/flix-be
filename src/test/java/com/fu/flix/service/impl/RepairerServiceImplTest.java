package com.fu.flix.service.impl;

import com.fu.flix.dao.RepairRequestDAO;
import com.fu.flix.dao.RepairerDAO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.dto.security.UserPrincipal;
import com.fu.flix.entity.RepairRequest;
import com.fu.flix.entity.Repairer;
import com.fu.flix.service.CustomerService;
import com.fu.flix.service.RepairerService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import static com.fu.flix.constant.Constant.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Slf4j
class RepairerServiceImplTest {

    @Autowired
    RepairerService underTest;
    @Autowired
    CustomerService customerService;
    @Autowired
    RepairerDAO repairerDAO;
    @Autowired
    RepairRequestDAO repairRequestDAO;
    String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Test
    public void test_approval_request_success() {
        // given
        String requestCode = createFixingRequestByCustomerId36();
        RepairerApproveRequest request = new RepairerApproveRequest();
        request.setRequestCode(requestCode);

        // when
        setUserContext(56L, "0865390056");
        RepairerApproveResponse response = underTest.approveRequest(request).getBody();

        // then
        Assertions.assertEquals(APPROVAL_REQUEST_SUCCESS, response.getMessage());
    }

    @Test
    public void test_approval_request_fail_when_request_code_is_invalid() {
        // given
        String requestCode = "34AEFEMWQGNN";
        RepairerApproveRequest request = new RepairerApproveRequest();
        request.setRequestCode(requestCode);

        // when
        setUserContext(56L, "0865390056");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.approveRequest(request).getBody());

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_approval_request_fail_when_request_code_is_empty() {
        // given
        String requestCode = "";
        RepairerApproveRequest request = new RepairerApproveRequest();
        request.setRequestCode(requestCode);

        // when
        setUserContext(56L, "0865390056");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.approveRequest(request).getBody());

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_approval_request_fail_when_request_code_is_null() {
        // given
        RepairerApproveRequest request = new RepairerApproveRequest();
        request.setRequestCode(null);

        // when
        setUserContext(56L, "0865390056");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.approveRequest(request).getBody());

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_approval_request_fail_when_request_does_not_pending() {
        // given
        String requestCode = "110722UZB1V6";
        RepairerApproveRequest request = new RepairerApproveRequest();
        request.setRequestCode(requestCode);

        // when
        setUserContext(56L, "0865390056");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.approveRequest(request).getBody());

        // then
        Assertions.assertEquals(JUST_CAN_ACCEPT_PENDING_REQUEST, exception.getMessage());
    }

    @Test
    public void test_approval_request_fail_when_repairer_on_another_fixing() {
        // given
        String requestCode = createFixingRequestByCustomerId36();
        RepairerApproveRequest request = new RepairerApproveRequest();
        request.setRequestCode(requestCode);

        // when
        setUserContext(56L, "0865390056");
        Repairer repairer = repairerDAO.findByUserId(56L).get();
        repairer.setRepairing(true);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.approveRequest(request).getBody());

        // then
        Assertions.assertEquals(CAN_NOT_ACCEPT_REQUEST_WHEN_ON_ANOTHER_FIXING, exception.getMessage());
    }

    @Test
    public void test_approval_request_fail_when_repairer_balance_not_enough() {
        // given
        String requestCode = createFixingRequestByCustomerId36();
        RepairerApproveRequest request = new RepairerApproveRequest();
        request.setRequestCode(requestCode);

        // when
        setUserContext(373L, "0865390056");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.approveRequest(request).getBody());

        // then
        Assertions.assertEquals(BALANCE_MUST_GREATER_THAN_OR_EQUAL_ + "3150", exception.getMessage());
    }

    @Test
    public void test_get_repairer_request_detail_success() {
        // given
        RequestingDetailForRepairerRequest request = new RequestingDetailForRepairerRequest();
        request.setRequestCode("1107226GDG5F");

        // when
        setUserContext(52L, "0865390056");
        RequestingDetailForRepairerResponse response = underTest.getRepairRequestDetail(request).getBody();

        // then
        Assertions.assertEquals(36L, response.getCustomerId());
    }

    @Test
    public void test_get_repairer_request_detail_fail_when_request_code_is_null() {
        // given
        RequestingDetailForRepairerRequest request = new RequestingDetailForRepairerRequest();
        request.setRequestCode(null);

        // when
        setUserContext(52L, "0865390056");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getRepairRequestDetail(request).getBody());

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_get_repairer_request_detail_fail_when_request_code_is_empty() {
        // given
        RequestingDetailForRepairerRequest request = new RequestingDetailForRepairerRequest();
        request.setRequestCode("");

        // when
        setUserContext(52L, "0865390056");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getRepairRequestDetail(request).getBody());

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void cancel_fixing_request_success() {
        // given
        String requestCode = createFixingRequestByCustomerId36();
        approvalRequestByRepairerId56(requestCode);

        CancelRequestForRepairerRequest request = new CancelRequestForRepairerRequest();
        request.setRequestCode(requestCode);
        request.setReason("Bận quá");

        // when
        setUserContext(56L, "0865390056");
        CancelRequestForRepairerResponse response = underTest.cancelFixingRequest(request).getBody();

        // then
        Assertions.assertEquals(CANCEL_REPAIR_REQUEST_SUCCESSFUL, response.getMessage());
    }

    @Test
    public void cancel_fixing_request_success_when_reason_is_null() {
        // given
        String requestCode = createFixingRequestByCustomerId36();
        approvalRequestByRepairerId56(requestCode);

        CancelRequestForRepairerRequest request = new CancelRequestForRepairerRequest();
        request.setRequestCode(requestCode);
        request.setReason(null);

        // when
        setUserContext(56L, "0865390056");
        CancelRequestForRepairerResponse response = underTest.cancelFixingRequest(request).getBody();

        // then
        Assertions.assertEquals(CANCEL_REPAIR_REQUEST_SUCCESSFUL, response.getMessage());
    }

    @Test
    public void cancel_fixing_request_success_when_reason_is_empty() {
        // given
        String requestCode = createFixingRequestByCustomerId36();
        approvalRequestByRepairerId56(requestCode);

        CancelRequestForRepairerRequest request = new CancelRequestForRepairerRequest();
        request.setRequestCode(requestCode);
        request.setReason("");

        // when
        setUserContext(56L, "0865390056");
        CancelRequestForRepairerResponse response = underTest.cancelFixingRequest(request).getBody();

        // then
        Assertions.assertEquals(CANCEL_REPAIR_REQUEST_SUCCESSFUL, response.getMessage());
    }

    @Test
    public void cancel_fixing_request_fail_when_request_code_is_invalid() {
        // given
        CancelRequestForRepairerRequest request = new CancelRequestForRepairerRequest();
        request.setRequestCode("ASDDSA324934");
        request.setReason("");

        // when
        setUserContext(56L, "0865390056");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.cancelFixingRequest(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void cancel_fixing_request_fail_when_request_code_is_null() {
        // given
        CancelRequestForRepairerRequest request = new CancelRequestForRepairerRequest();
        request.setRequestCode(null);
        request.setReason("");

        // when
        setUserContext(56L, "0865390056");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.cancelFixingRequest(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void cancel_fixing_request_fail_when_request_code_is_empty() {
        // given
        CancelRequestForRepairerRequest request = new CancelRequestForRepairerRequest();
        request.setRequestCode("");
        request.setReason("");

        // when
        setUserContext(56L, "0865390056");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.cancelFixingRequest(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void cancel_fixing_request_fail_when_repairer_does_not_have_permission_to_cancel_request() {
        // given
        String requestCode = createFixingRequestByCustomerId36();
        approvalRequestByRepairerId56(requestCode);

        CancelRequestForRepairerRequest request = new CancelRequestForRepairerRequest();
        request.setRequestCode(requestCode);
        request.setReason(null);

        // when
        setUserContext(52L, "0865390056");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.cancelFixingRequest(request));

        // then
        Assertions.assertEquals(USER_DOES_NOT_HAVE_PERMISSION_TO_CANCEL_THIS_REQUEST, exception.getMessage());
    }

    @Test
    public void cancel_fixing_request_fail_when_request_does_not_fixing_or_approval() {
        // given
        String requestCode = createFixingRequestByCustomerId36();
        approvalRequestByRepairerId56(requestCode);
        RepairRequest repairRequest = repairRequestDAO.findByRequestCode(requestCode).get();
        repairRequest.setStatusId("PW");

        CancelRequestForRepairerRequest request = new CancelRequestForRepairerRequest();
        request.setRequestCode(requestCode);
        request.setReason(null);

        // when
        setUserContext(56L, "0865390056");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.cancelFixingRequest(request));

        // then
        Assertions.assertEquals(ONLY_CAN_CANCEL_REQUEST_FIXING_OR_APPROVED, exception.getMessage());
    }

    private String createFixingRequestByCustomerId36() {
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

        RequestingRepairResponse response = customerService.createFixingRequest(request).getBody();
        return response.getRequestCode();
    }

    private void approvalRequestByRepairerId56(String requestCode) {
        setUserContext(56L, "0865390056");
        RepairerApproveRequest request = new RepairerApproveRequest();
        request.setRequestCode(requestCode);
        underTest.approveRequest(request);
    }

    @Test
    public void test_get_fixing_request_history_success_when_status_is_CANCELLED() {
        // given
        HistoryRequestForRepairerRequest request = new HistoryRequestForRepairerRequest();
        request.setStatus("CANCELLED");

        // when
        setUserContext(52L, "0865390037");
        HistoryRequestForRepairerResponse response = underTest.getFixingRequestHistories(request).getBody();

        // then
        Assertions.assertTrue(response.getRequestHistories().size() > 0);

    }

    @Test
    public void test_get_fixing_request_history_success_when_status_is_DONE() {
        // given
        HistoryRequestForRepairerRequest request = new HistoryRequestForRepairerRequest();
        request.setStatus("DONE");

        // when
        setUserContext(52L, "0865390037");
        HistoryRequestForRepairerResponse response = underTest.getFixingRequestHistories(request).getBody();

        // then
        Assertions.assertTrue(response.getRequestHistories().size() > 0);
    }

    @Test
    public void test_get_fixing_request_history_success_when_status_is_PENDING() {
        // given
        HistoryRequestForRepairerRequest request = new HistoryRequestForRepairerRequest();
        request.setStatus("PENDING");

        // when
        setUserContext(52L, "0865390037");
        HistoryRequestForRepairerResponse response = underTest.getFixingRequestHistories(request).getBody();

        // then
        Assertions.assertEquals(0, response.getRequestHistories().size());
    }

    @Test
    public void test_get_fixing_request_history_success_when_status_is_PAYMENT_WAITING() {
        // given
        HistoryRequestForRepairerRequest request = new HistoryRequestForRepairerRequest();
        request.setStatus("PAYMENT_WAITING");

        // when
        setUserContext(52L, "0865390037");
        HistoryRequestForRepairerResponse response = underTest.getFixingRequestHistories(request).getBody();

        // then
        Assertions.assertNotNull(response.getRequestHistories());
    }

    @Test
    public void test_get_fixing_request_history_fail_when_status_is_empty() {
        // given
        HistoryRequestForRepairerRequest request = new HistoryRequestForRepairerRequest();
        request.setStatus("");

        // when
        setUserContext(52L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getFixingRequestHistories(request));

        // then
        Assertions.assertEquals(INVALID_STATUS, exception.getMessage());
    }

    @Test
    public void test_get_fixing_request_history_fail_when_status_is_null() {
        // given
        HistoryRequestForRepairerRequest request = new HistoryRequestForRepairerRequest();
        request.setStatus(null);

        // when
        setUserContext(52L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getFixingRequestHistories(request));

        // then
        Assertions.assertEquals(INVALID_STATUS, exception.getMessage());
    }

    @Test
    public void test_confirm_fixing_success() {
        // given
        String requestCode = createFixingRequestByCustomerId36();
        approvalRequestByRepairerId56(requestCode);
        ConfirmFixingRequest request = new ConfirmFixingRequest();
        request.setRequestCode(requestCode);
        setUserContext(56L, "0865390037");

        // when
        ConfirmFixingResponse response = underTest.confirmFixing(request).getBody();

        // then
        Assertions.assertEquals(CONFIRM_FIXING_SUCCESS, response.getMessage());
    }

    @Test
    public void test_confirm_fixing_fail_when_request_code_is_invalid() {
        // given
        ConfirmFixingRequest request = new ConfirmFixingRequest();
        request.setRequestCode("1INVALIDCODE");
        setUserContext(56L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmFixing(request).getBody());

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_confirm_fixing_fail_when_request_code_is_empty() {
        // given
        ConfirmFixingRequest request = new ConfirmFixingRequest();
        request.setRequestCode("");
        setUserContext(56L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmFixing(request).getBody());

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_confirm_fixing_fail_when_request_code_is_null() {
        // given
        ConfirmFixingRequest request = new ConfirmFixingRequest();
        request.setRequestCode(null);
        setUserContext(56L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmFixing(request).getBody());

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_confirm_fixing_fail_when_request_status_is_not_APPROVAL() {
        // given
        String requestCode = createFixingRequestByCustomerId36();
        ConfirmFixingRequest request = new ConfirmFixingRequest();
        request.setRequestCode(requestCode);
        setUserContext(56L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmFixing(request).getBody());

        // then
        Assertions.assertEquals(JUST_CAN_CONFIRM_FIXING_WHEN_REQUEST_STATUS_APPROVED, exception.getMessage());
    }

    @Test
    public void test_confirm_fixing_fail_when_repairer_does_not_have_permission() {
        // given
        String requestCode = createFixingRequestByCustomerId36();
        approvalRequestByRepairerId56(requestCode);
        ConfirmFixingRequest request = new ConfirmFixingRequest();
        request.setRequestCode(requestCode);
        setUserContext(52L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmFixing(request).getBody());

        // then
        Assertions.assertEquals(USER_DOES_NOT_HAVE_PERMISSION_TO_CONFIRM_FIXING_THIS_REQUEST, exception.getMessage());
    }

    @Test
    public void test_confirm_fixing_fail_when_repairer_on_another_fixing() {
        // given
        String requestCode = createFixingRequestByCustomerId36();
        approvalRequestByRepairerId56(requestCode);
        ConfirmFixingRequest request = new ConfirmFixingRequest();
        request.setRequestCode(requestCode);

        long userId = 56L;
        Repairer repairer = repairerDAO.findByUserId(56L).get();
        repairer.setRepairing(true);
        setUserContext(userId, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmFixing(request).getBody());

        // then
        Assertions.assertEquals(CAN_NOT_CONFIRM_FIXING_WHEN_ON_ANOTHER_FIXING, exception.getMessage());
    }

    private void confirmFixingByRepairerId56(String requestCode) {
        ConfirmFixingRequest request = new ConfirmFixingRequest();
        request.setRequestCode(requestCode);
        setUserContext(56L, "0865390037");
        underTest.confirmFixing(request);
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