package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.dao.BalanceDAO;
import com.fu.flix.dao.InvoiceDAO;
import com.fu.flix.dao.RepairRequestDAO;
import com.fu.flix.dao.RepairerDAO;
import com.fu.flix.dto.ExtraServiceInputDTO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.dto.security.UserPrincipal;
import com.fu.flix.entity.Balance;
import com.fu.flix.entity.Invoice;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    @Autowired
    BalanceDAO balanceDAO;
    @Autowired
    AppConf appConf;

    @Autowired
    InvoiceDAO invoiceDAO;

    @Test
    public void test_approval_request_success() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        RepairerApproveRequest request = new RepairerApproveRequest();
        request.setRequestCode(requestCode);

        // when
        setRepairerContext(56L, "0865390056");
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
        setRepairerContext(56L, "0865390056");
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
        setRepairerContext(56L, "0865390056");
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
        setRepairerContext(56L, "0865390056");
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
        setRepairerContext(56L, "0865390056");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.approveRequest(request).getBody());

        // then
        Assertions.assertEquals(JUST_CAN_ACCEPT_PENDING_REQUEST, exception.getMessage());
    }

    @Test
    public void test_approval_request_fail_when_repairer_on_another_fixing() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        RepairerApproveRequest request = new RepairerApproveRequest();
        request.setRequestCode(requestCode);

        // when
        setRepairerContext(56L, "0865390056");
        Repairer repairer = repairerDAO.findByUserId(56L).get();
        repairer.setRepairing(true);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.approveRequest(request).getBody());

        // then
        Assertions.assertEquals(CAN_NOT_ACCEPT_REQUEST_WHEN_ON_ANOTHER_FIXING, exception.getMessage());
    }

    @Test
    public void test_approval_request_fail_when_repairer_balance_not_enough() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        RepairerApproveRequest request = new RepairerApproveRequest();
        request.setRequestCode(requestCode);

        // when
        setRepairerContext(373L, "0865390056");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.approveRequest(request).getBody());

        // then
        Assertions.assertEquals(BALANCE_MUST_GREATER_THAN_OR_EQUAL_ + appConf.getMilestoneMoney(), exception.getMessage());
    }

    @Test
    public void test_get_repairer_request_detail_success() {
        // given
        RequestingDetailForRepairerRequest request = new RequestingDetailForRepairerRequest();
        request.setRequestCode("1107226GDG5F");

        // when
        setRepairerContext(52L, "0865390056");
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
        setRepairerContext(52L, "0865390056");
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
        setRepairerContext(52L, "0865390056");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getRepairRequestDetail(request).getBody());

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void cancel_fixing_request_success() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);

        CancelRequestForRepairerRequest request = new CancelRequestForRepairerRequest();
        request.setRequestCode(requestCode);
        request.setReason("Bận quá");

        // when
        setRepairerContext(56L, "0865390056");
        CancelRequestForRepairerResponse response = underTest.cancelFixingRequest(request).getBody();

        // then
        Assertions.assertEquals(CANCEL_REPAIR_REQUEST_SUCCESSFUL, response.getMessage());
    }

    @Test
    public void cancel_fixing_request_success_when_reason_is_null() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);

        CancelRequestForRepairerRequest request = new CancelRequestForRepairerRequest();
        request.setRequestCode(requestCode);
        request.setReason(null);

        // when
        setRepairerContext(56L, "0865390056");
        CancelRequestForRepairerResponse response = underTest.cancelFixingRequest(request).getBody();

        // then
        Assertions.assertEquals(CANCEL_REPAIR_REQUEST_SUCCESSFUL, response.getMessage());
    }

    @Test
    public void cancel_fixing_request_success_when_reason_is_empty() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);

        CancelRequestForRepairerRequest request = new CancelRequestForRepairerRequest();
        request.setRequestCode(requestCode);
        request.setReason("");

        // when
        setRepairerContext(56L, "0865390056");
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
        setRepairerContext(56L, "0865390056");
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
        setRepairerContext(56L, "0865390056");
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
        setRepairerContext(56L, "0865390056");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.cancelFixingRequest(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void cancel_fixing_request_fail_when_repairer_does_not_have_permission_to_cancel_request() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);

        CancelRequestForRepairerRequest request = new CancelRequestForRepairerRequest();
        request.setRequestCode(requestCode);
        request.setReason(null);

        // when
        setRepairerContext(52L, "0865390056");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.cancelFixingRequest(request));

        // then
        Assertions.assertEquals(USER_DOES_NOT_HAVE_PERMISSION_TO_CANCEL_THIS_REQUEST, exception.getMessage());
    }

    @Test
    public void cancel_fixing_request_fail_when_request_does_not_fixing_or_approval() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);
        RepairRequest repairRequest = repairRequestDAO.findByRequestCode(requestCode).get();
        repairRequest.setStatusId("PW");

        CancelRequestForRepairerRequest request = new CancelRequestForRepairerRequest();
        request.setRequestCode(requestCode);
        request.setReason(null);

        // when
        setRepairerContext(56L, "0865390056");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.cancelFixingRequest(request));

        // then
        Assertions.assertEquals(ONLY_CAN_CANCEL_REQUEST_FIXING_OR_APPROVED, exception.getMessage());
    }

    private void approvalRequestByRepairerId56(String requestCode) throws IOException {
        setRepairerContext(56L, "0865390056");
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
        setRepairerContext(52L, "0865390037");
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
        setRepairerContext(52L, "0865390037");
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
        setRepairerContext(52L, "0865390037");
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
        setRepairerContext(52L, "0865390037");
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
        setRepairerContext(52L, "0865390037");
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
        setRepairerContext(52L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.getFixingRequestHistories(request));

        // then
        Assertions.assertEquals(INVALID_STATUS, exception.getMessage());
    }

    @Test
    public void test_confirm_fixing_success() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);
        ConfirmFixingRequest request = new ConfirmFixingRequest();
        request.setRequestCode(requestCode);
        setRepairerContext(56L, "0865390037");

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
        setRepairerContext(56L, "0865390037");

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
        setRepairerContext(56L, "0865390037");

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
        setRepairerContext(56L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmFixing(request).getBody());

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_confirm_fixing_fail_when_request_status_is_not_APPROVAL() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        ConfirmFixingRequest request = new ConfirmFixingRequest();
        request.setRequestCode(requestCode);
        setRepairerContext(56L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmFixing(request).getBody());

        // then
        Assertions.assertEquals(JUST_CAN_CONFIRM_FIXING_WHEN_REQUEST_STATUS_APPROVED, exception.getMessage());
    }

    @Test
    public void test_confirm_fixing_fail_when_repairer_does_not_have_permission() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);
        ConfirmFixingRequest request = new ConfirmFixingRequest();
        request.setRequestCode(requestCode);
        setRepairerContext(52L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmFixing(request).getBody());

        // then
        Assertions.assertEquals(USER_DOES_NOT_HAVE_PERMISSION_TO_CONFIRM_FIXING_THIS_REQUEST, exception.getMessage());
    }

    @Test
    public void test_confirm_fixing_fail_when_repairer_on_another_fixing() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);
        ConfirmFixingRequest request = new ConfirmFixingRequest();
        request.setRequestCode(requestCode);

        long userId = 56L;
        Repairer repairer = repairerDAO.findByUserId(56L).get();
        repairer.setRepairing(true);
        setRepairerContext(userId, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmFixing(request));

        // then
        Assertions.assertEquals(CAN_NOT_CONFIRM_FIXING_WHEN_ON_ANOTHER_FIXING, exception.getMessage());
    }

    @Test
    public void test_create_invoice_success() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService3();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setRequestCode(requestCode);
        setRepairerContext(56L, "0865390037");

        // when
        CreateInvoiceResponse response = underTest.createInvoice(request).getBody();

        // then
        Assertions.assertEquals(CREATE_INVOICE_SUCCESS, response.getMessage());
    }

    @Test
    public void test_create_invoice_fail_when_repairer_does_not_have_permission() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService3();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setRequestCode(requestCode);
        setRepairerContext(52L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createInvoice(request));

        // then
        Assertions.assertEquals(REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_CREATE_INVOICE_FOR_THIS_REQUEST, exception.getMessage());
    }

    @Test
    public void test_create_invoice_fail_when_request_code_is_invalid() {
        // given
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setRequestCode("INVALID123");
        setRepairerContext(52L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createInvoice(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_create_invoice_fail_when_request_code_is_null() {
        // given
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setRequestCode(null);
        setRepairerContext(52L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createInvoice(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_create_invoice_fail_when_request_code_is_empty() {
        // given
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setRequestCode("");
        setRepairerContext(52L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createInvoice(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_create_invoice_fail_when_balance_less_than_comission() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService3();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setRequestCode(requestCode);
        setRepairerContext(56L, "0865390037");

        Balance balance = balanceDAO.findByUserId(56L).get();
        balance.setBalance(0L);

        Invoice invoice = invoiceDAO.findByRequestCode(requestCode).get();
        Long commission = getCommission(invoice);
        long requiredMoney = commission + appConf.getMilestoneMoney();

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createInvoice(request));

        // then
        Assertions.assertEquals(BALANCE_MUST_GREATER_THAN_OR_EQUAL_ + requiredMoney, exception.getMessage());
    }

    private Long getCommission(Invoice invoice) {
        return (long) (invoice.getActualProceeds() * this.appConf.getProfitRate()) + invoice.getVatPrice();
    }


    @Test
    public void test_confirm_invoice_paid_success() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService3();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);
        createInvoiceByRepairerId56(requestCode);
        ConfirmInvoicePaidRequest request = new ConfirmInvoicePaidRequest();
        request.setRequestCode(requestCode);
        setRepairerContext(56L, "0865390037");

        // when
        ConfirmInvoicePaidResponse response = underTest.confirmInvoicePaid(request).getBody();

        // then
        Assertions.assertEquals(CONFIRM_INVOICE_PAID_SUCCESS, response.getMessage());
    }

    @Test
    public void test_confirm_invoice_paid_fail_when_invalid_request_code() {
        // given
        ConfirmInvoicePaidRequest request = new ConfirmInvoicePaidRequest();
        request.setRequestCode("INVALID");
        setRepairerContext(56L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmInvoicePaid(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_confirm_invoice_paid_fail_when_payment_is_not_cash() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForVNPAY();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);
        createInvoiceByRepairerId56(requestCode);
        ConfirmInvoicePaidRequest request = new ConfirmInvoicePaidRequest();
        request.setRequestCode(requestCode);
        setRepairerContext(56L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmInvoicePaid(request));

        // then
        Assertions.assertEquals(CONFIRM_INVOICE_PAID_ONLY_USE_FOR_PAYMENT_IN_CASH, exception.getMessage());
    }

    @Test
    public void test_confirm_invoice_paid_fail_when_status_is_not_PW() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);
        ConfirmInvoicePaidRequest request = new ConfirmInvoicePaidRequest();
        request.setRequestCode(requestCode);
        setRepairerContext(56L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmInvoicePaid(request));

        // then
        Assertions.assertEquals(CONFIRM_INVOICE_PAID_ONLY_USE_WHEN_STATUS_IS_PAYMENT_WAITING, exception.getMessage());
    }

    @Test
    public void test_confirm_invoice_paid_fail_when_repairer_does_not_permission() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService3();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);
        createInvoiceByRepairerId56(requestCode);
        ConfirmInvoicePaidRequest request = new ConfirmInvoicePaidRequest();
        request.setRequestCode(requestCode);
        setRepairerContext(52L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmInvoicePaid(request));

        // then
        Assertions.assertEquals(USER_DOES_NOT_HAVE_PERMISSION_TO_CONFIRM_PAID_THIS_INVOICE, exception.getMessage());
    }

    @Test
    public void test_put_accessories_to_invoice_success() throws IOException {
        // given
        List<Long> accessoryIds = new ArrayList<>();
        accessoryIds.add(1L);
        accessoryIds.add(2L);

        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);

        AddAccessoriesToInvoiceRequest request = new AddAccessoriesToInvoiceRequest();
        request.setRequestCode(requestCode);
        request.setAccessoryIds(accessoryIds);

        setRepairerContext(56L, "0865390056");

        // when
        AddAccessoriesToInvoiceResponse response = underTest.putAccessoriesToInvoice(request).getBody();

        // then
        Assertions.assertEquals(PUT_ACCESSORIES_TO_INVOICE_SUCCESS, response.getMessage());
    }

    @Test
    public void test_put_accessories_to_invoice_fail_when_status_is_not_fixing() throws IOException {
        // given
        List<Long> accessoryIds = new ArrayList<>();
        accessoryIds.add(1L);
        accessoryIds.add(2L);

        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);

        AddAccessoriesToInvoiceRequest request = new AddAccessoriesToInvoiceRequest();
        request.setRequestCode(requestCode);
        request.setAccessoryIds(accessoryIds);

        setRepairerContext(56L, "0865390056");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.putAccessoriesToInvoice(request));

        // then
        Assertions.assertEquals(JUST_CAN_ADD_ACCESSORIES_WHEN_REQUEST_STATUS_IS_FIXING, exception.getMessage());
    }

    @Test
    public void test_put_accessories_to_invoice_fail_when_repairer_does_not_have_permission() throws IOException {
        // given
        List<Long> accessoryIds = new ArrayList<>();
        accessoryIds.add(1L);
        accessoryIds.add(2L);

        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);

        AddAccessoriesToInvoiceRequest request = new AddAccessoriesToInvoiceRequest();
        request.setRequestCode(requestCode);
        request.setAccessoryIds(accessoryIds);

        setRepairerContext(52L, "0865390056");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.putAccessoriesToInvoice(request));

        // then
        Assertions.assertEquals(REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_ADD_ACCESSORIES_FOR_THIS_INVOICE, exception.getMessage());
    }

    @Test
    public void test_put_accessories_to_invoice_fail_when_request_code_is_null() {
        // given
        List<Long> accessoryIds = new ArrayList<>();
        accessoryIds.add(1L);
        accessoryIds.add(2L);

        AddAccessoriesToInvoiceRequest request = new AddAccessoriesToInvoiceRequest();
        request.setRequestCode(null);
        request.setAccessoryIds(accessoryIds);

        setRepairerContext(56L, "0865390056");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.putAccessoriesToInvoice(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_put_accessories_to_invoice_success_when_list_sub_service_is_null() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);

        AddAccessoriesToInvoiceRequest request = new AddAccessoriesToInvoiceRequest();
        request.setRequestCode(requestCode);
        request.setAccessoryIds(null);

        setRepairerContext(56L, "0865390056");

        // when
        AddAccessoriesToInvoiceResponse response = underTest.putAccessoriesToInvoice(request).getBody();

        // then
        Assertions.assertEquals(PUT_ACCESSORIES_TO_INVOICE_SUCCESS, response.getMessage());
    }

    @Test
    public void test_put_subService_to_invoice_success() throws IOException {
        // given
        List<Long> serviceIds = new ArrayList<>();
        serviceIds.add(1L);
        serviceIds.add(2L);

        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);

        AddSubServicesToInvoiceRequest request = new AddSubServicesToInvoiceRequest();
        request.setRequestCode(requestCode);
        request.setSubServiceIds(serviceIds);

        setRepairerContext(56L, "0865390056");

        // when
        AddSubServicesToInvoiceResponse response = underTest.putSubServicesToInvoice(request).getBody();

        // then
        Assertions.assertEquals(PUT_SUB_SERVICE_TO_INVOICE_SUCCESS, response.getMessage());
    }

    @Test
    public void test_put_subService_to_invoice_fail_when_status_is_not_fixing() throws IOException {
        // given
        List<Long> serviceIds = new ArrayList<>();
        serviceIds.add(1L);
        serviceIds.add(2L);

        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);

        AddSubServicesToInvoiceRequest request = new AddSubServicesToInvoiceRequest();
        request.setRequestCode(requestCode);
        request.setSubServiceIds(serviceIds);

        setRepairerContext(56L, "0865390056");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.putSubServicesToInvoice(request));

        // then
        Assertions.assertEquals(JUST_CAN_ADD_SUB_SERVICES_WHEN_REQUEST_STATUS_IS_FIXING, exception.getMessage());
    }

    @Test
    public void test_put_subService_to_invoice_fail_when_repairer_does_not_have_permission() throws IOException {
        // given
        List<Long> serviceIds = new ArrayList<>();
        serviceIds.add(1L);
        serviceIds.add(2L);

        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);

        AddSubServicesToInvoiceRequest request = new AddSubServicesToInvoiceRequest();
        request.setRequestCode(requestCode);
        request.setSubServiceIds(serviceIds);

        setRepairerContext(52L, "0865390056");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.putSubServicesToInvoice(request));

        // then
        Assertions.assertEquals(REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_ADD_SUB_SERVICES_FOR_THIS_INVOICE, exception.getMessage());
    }

    @Test
    public void test_put_subService_to_invoice_fail_when_request_code_is_null() {
        // given
        List<Long> serviceIds = new ArrayList<>();
        serviceIds.add(1L);
        serviceIds.add(2L);

        AddSubServicesToInvoiceRequest request = new AddSubServicesToInvoiceRequest();
        request.setRequestCode(null);
        request.setSubServiceIds(serviceIds);

        setRepairerContext(56L, "0865390056");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.putSubServicesToInvoice(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_put_subService_to_invoice_success_when_list_sub_service_is_null() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);

        AddSubServicesToInvoiceRequest request = new AddSubServicesToInvoiceRequest();
        request.setRequestCode(requestCode);
        request.setSubServiceIds(null);

        setRepairerContext(56L, "0865390056");

        // when
        AddSubServicesToInvoiceResponse response = underTest.putSubServicesToInvoice(request).getBody();

        // then
        Assertions.assertEquals(PUT_SUB_SERVICE_TO_INVOICE_SUCCESS, response.getMessage());
    }

    @Test
    public void test_put_extra_services_success() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);

        List<ExtraServiceInputDTO> extraServices = new ArrayList<>();
        extraServices.add(new ExtraServiceInputDTO("Tiền lau WC", null, 25000L, null));
        extraServices.add(new ExtraServiceInputDTO("Tiền thổi kèn", "meo meo", 25000L, 3));

        AddExtraServiceToInvoiceRequest request = new AddExtraServiceToInvoiceRequest();
        request.setRequestCode(requestCode);
        request.setExtraServices(extraServices);

        setRepairerContext(56L, "0865390056");

        // when
        AddExtraServiceToInvoiceResponse response = underTest.putExtraServicesToInvoice(request).getBody();

        // then
        Assertions.assertEquals(PUT_EXTRA_SERVICE_TO_INVOICE_SUCCESS, response.getMessage());
    }

    @Test
    public void test_put_extra_services_success_when_extra_list_is_null() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);

        AddExtraServiceToInvoiceRequest request = new AddExtraServiceToInvoiceRequest();
        request.setRequestCode(requestCode);
        request.setExtraServices(null);

        setRepairerContext(56L, "0865390056");

        // when
        AddExtraServiceToInvoiceResponse response = underTest.putExtraServicesToInvoice(request).getBody();

        // then
        Assertions.assertEquals(PUT_EXTRA_SERVICE_TO_INVOICE_SUCCESS, response.getMessage());
    }

    @Test
    public void test_put_extra_services_fail_when_extra_service_have_name_is_null() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);

        List<ExtraServiceInputDTO> extraServices = new ArrayList<>();
        extraServices.add(new ExtraServiceInputDTO(null, null, 25000L, null));
        extraServices.add(new ExtraServiceInputDTO("Tiền thổi kèn", "meo meo", 25000L, 3));

        AddExtraServiceToInvoiceRequest request = new AddExtraServiceToInvoiceRequest();
        request.setRequestCode(requestCode);
        request.setExtraServices(extraServices);

        setRepairerContext(56L, "0865390056");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.putExtraServicesToInvoice(request));

        // then
        Assertions.assertEquals(LIST_EXTRA_SERVICES_CONTAIN_INVALID_ELEMENT, exception.getMessage());
    }

    @Test
    public void test_put_extra_services_fail_when_extra_service_have_description_length_greater_than_2500() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);

        List<ExtraServiceInputDTO> extraServices = new ArrayList<>();
        extraServices.add(new ExtraServiceInputDTO("meo meo", "a".repeat(2501), 25000L, null));
        extraServices.add(new ExtraServiceInputDTO("Tiền thổi kèn", "meo meo", 25000L, 3));

        AddExtraServiceToInvoiceRequest request = new AddExtraServiceToInvoiceRequest();
        request.setRequestCode(requestCode);
        request.setExtraServices(extraServices);

        setRepairerContext(56L, "0865390056");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.putExtraServicesToInvoice(request));

        // then
        Assertions.assertEquals(LIST_EXTRA_SERVICES_CONTAIN_INVALID_ELEMENT, exception.getMessage());
    }

    @Test
    public void test_put_extra_services_fail_when_extra_service_have_price_is_null() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);

        List<ExtraServiceInputDTO> extraServices = new ArrayList<>();
        extraServices.add(new ExtraServiceInputDTO("meo meo", null, null, null));
        extraServices.add(new ExtraServiceInputDTO("Tiền thổi kèn", "meo meo", 25000L, 3));

        AddExtraServiceToInvoiceRequest request = new AddExtraServiceToInvoiceRequest();
        request.setRequestCode(requestCode);
        request.setExtraServices(extraServices);

        setRepairerContext(56L, "0865390056");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.putExtraServicesToInvoice(request));

        // then
        Assertions.assertEquals(LIST_EXTRA_SERVICES_CONTAIN_INVALID_ELEMENT, exception.getMessage());
    }

    @Test
    public void test_put_extra_services_fail_when_status_is_not_fixing() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);

        List<ExtraServiceInputDTO> extraServices = new ArrayList<>();
        extraServices.add(new ExtraServiceInputDTO("meo meo", null, 25000L, null));
        extraServices.add(new ExtraServiceInputDTO("Tiền thổi kèn", "meo meo", 25000L, 3));

        AddExtraServiceToInvoiceRequest request = new AddExtraServiceToInvoiceRequest();
        request.setRequestCode(requestCode);
        request.setExtraServices(extraServices);

        setRepairerContext(56L, "0865390056");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.putExtraServicesToInvoice(request));

        // then
        Assertions.assertEquals(JUST_CAN_ADD_EXTRA_SERVICE_WHEN_REQUEST_STATUS_IS_FIXING, exception.getMessage());
    }

    @Test
    public void test_put_extra_services_fail_when_repairer_does_not_have_permission() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);
        confirmFixingByRepairerId56(requestCode);

        List<ExtraServiceInputDTO> extraServices = new ArrayList<>();
        extraServices.add(new ExtraServiceInputDTO("meo meo", null, 25000L, null));
        extraServices.add(new ExtraServiceInputDTO("Tiền thổi kèn", "meo meo", 25000L, 3));

        AddExtraServiceToInvoiceRequest request = new AddExtraServiceToInvoiceRequest();
        request.setRequestCode(requestCode);
        request.setExtraServices(extraServices);

        setRepairerContext(52L, "0865390056");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.putExtraServicesToInvoice(request));

        // then
        Assertions.assertEquals(REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_ADD_EXTRA_SERVICE_FOR_THIS_INVOICE, exception.getMessage());
    }

    @Test
    void test_requestWithdraw_success() {
        // given
        RepairerWithdrawRequest request = new RepairerWithdrawRequest();
        request.setAmount(35000L);
        request.setWithdrawType("BANKING");
        request.setBankCode("TPBANK");
        request.setBankAccountNumber("12345678");
        request.setBankAccountName("CHI DUNG");

        setRepairerContext(56L, "0865390056");

        // when
        RepairerWithdrawResponse response = underTest.requestWithdraw(request).getBody();

        // then
        Assertions.assertEquals(CREATE_REQUEST_WITHDRAW_SUCCESS, response.getMessage());
    }

    @Test
    void test_requestWithdraw_fail_when_invalid_withdraw_type() {
        // given
        RepairerWithdrawRequest request = new RepairerWithdrawRequest();
        request.setAmount(35000L);
        request.setWithdrawType(null);
        request.setBankCode("TPBANK");
        request.setBankAccountNumber("12345678");
        request.setBankAccountName("CHI DUNG");

        setRepairerContext(56L, "0865390056");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.requestWithdraw(request));

        // then
        Assertions.assertEquals(INVALID_WITHDRAW_TYPE, exception.getMessage());
    }

    @Test
    void test_requestWithdraw_fail_when_amount_less_than_5000() {
        // given
        RepairerWithdrawRequest request = new RepairerWithdrawRequest();
        request.setAmount(4500L);
        request.setWithdrawType("BANKING");
        request.setBankCode("TPBANK");
        request.setBankAccountNumber("12345678");
        request.setBankAccountName("CHI DUNG");

        setRepairerContext(56L, "0865390056");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.requestWithdraw(request));

        // then
        Assertions.assertEquals(AMOUNT_MUST_BE_GREATER_OR_EQUAL_ + appConf.getMinVnPay(), exception.getMessage());
    }

    @Test
    void test_requestWithdraw_fail_when_balance_is_0() {
        // given
        RepairerWithdrawRequest request = new RepairerWithdrawRequest();
        request.setAmount(35000L);
        request.setWithdrawType("BANKING");
        request.setBankCode("TPBANK");
        request.setBankAccountNumber("12345678");
        request.setBankAccountName("CHI DUNG");

        balanceDAO.findByUserId(56L).get().setBalance(0L);
        setRepairerContext(56L, "0865390056");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.requestWithdraw(request));

        // then
        Assertions.assertEquals(BALANCE_NOT_ENOUGH, exception.getMessage());
    }

    @Test
    void test_requestWithdraw_fail_when_repairer_have_a_request_and_balance_not_enough() throws IOException {
        // given
        String requestCode = createFixingRequestByCustomerId36ForService1();
        approvalRequestByRepairerId56(requestCode);

        RepairerWithdrawRequest request = new RepairerWithdrawRequest();
        request.setAmount(35000L);
        request.setWithdrawType("BANKING");
        request.setBankCode("TPBANK");
        request.setBankAccountNumber("12345678");
        request.setBankAccountName("CHI DUNG");

        balanceDAO.findByUserId(56L).get().setBalance(0L);
        setRepairerContext(56L, "0865390056");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.requestWithdraw(request));

        // then
        Assertions.assertEquals(BALANCE_MUST_GREATER_THAN_OR_EQUAL_ + appConf.getMilestoneMoney(), exception.getMessage());
    }

    @Test
    void test_requestWithdraw_fail_when_bank_account_name_is_null() {
        // given
        RepairerWithdrawRequest request = new RepairerWithdrawRequest();
        request.setAmount(35000L);
        request.setWithdrawType("BANKING");
        request.setBankCode("TPBANK");
        request.setBankAccountNumber("12345678");
        request.setBankAccountName(null);

        setRepairerContext(56L, "0865390056");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.requestWithdraw(request));

        // then
        Assertions.assertEquals(INVALID_BANK_ACCOUNT_NAME, exception.getMessage());
    }

    @Test
    void test_requestWithdraw_fail_when_bank_account_number_is_null() {
        // given
        RepairerWithdrawRequest request = new RepairerWithdrawRequest();
        request.setAmount(35000L);
        request.setWithdrawType("BANKING");
        request.setBankCode("TPBANK");
        request.setBankAccountNumber(null);
        request.setBankAccountName("CHI DUNG");

        setRepairerContext(56L, "0865390056");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.requestWithdraw(request));

        // then
        Assertions.assertEquals(INVALID_BANK_ACCOUNT_NUMBER, exception.getMessage());
    }

    @Test
    void test_requestWithdraw_fail_when_bank_code_is_null() {
        // given
        RepairerWithdrawRequest request = new RepairerWithdrawRequest();
        request.setAmount(35000L);
        request.setWithdrawType("BANKING");
        request.setBankCode(null);
        request.setBankAccountNumber("12345678");
        request.setBankAccountName("CHI DUNG");

        setRepairerContext(56L, "0865390056");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.requestWithdraw(request));

        // then
        Assertions.assertEquals(INVALID_BANK_CODE, exception.getMessage());
    }

    @Test
    void test_requestWithdraw_success_when_withdraw_type_is_CASH() {
        // given
        RepairerWithdrawRequest request = new RepairerWithdrawRequest();
        request.setAmount(35000L);
        request.setWithdrawType("CASH");

        setRepairerContext(56L, "0865390056");

        // when
        RepairerWithdrawResponse response = underTest.requestWithdraw(request).getBody();

        // then
        Assertions.assertEquals(CREATE_REQUEST_WITHDRAW_SUCCESS, response.getMessage());
    }

    private void createInvoiceByRepairerId56(String requestCode) throws IOException {
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setRequestCode(requestCode);
        setRepairerContext(56L, "0865390037");
        underTest.createInvoice(request);
    }

    private void confirmFixingByRepairerId56(String requestCode) throws IOException {
        ConfirmFixingRequest request = new ConfirmFixingRequest();
        request.setRequestCode(requestCode);
        setRepairerContext(56L, "0865390037");
        underTest.confirmFixing(request);
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

    private String createFixingRequestByCustomerId36ForService1() throws IOException {
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

        setCustomerContext(36L, "0865390037");

        RequestingRepairResponse response = customerService.createFixingRequest(request).getBody();
        return response.getRequestCode();
    }

    private String createFixingRequestByCustomerId36ForVNPAY() throws IOException {
        Long serviceId = 1L;
        Long addressId = 7L;
        String expectFixingDay = DateFormatUtil.toString(LocalDateTime.now().plusDays(2L), DATE_TIME_PATTERN);
        String description = "Thợ phải đẹp trai";
        String paymentMethodId = "V";

        RequestingRepairRequest request = new RequestingRepairRequest();
        request.setServiceId(serviceId);
        request.setDescription(description);
        request.setExpectFixingDay(expectFixingDay);
        request.setAddressId(addressId);
        request.setPaymentMethodId(paymentMethodId);

        setCustomerContext(36L, "0865390037");

        RequestingRepairResponse response = customerService.createFixingRequest(request).getBody();
        return response.getRequestCode();
    }

    private String createFixingRequestByCustomerId36ForService3() throws IOException {
        Long serviceId = 3L;
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

        setCustomerContext(36L, "0865390037");

        RequestingRepairResponse response = customerService.createFixingRequest(request).getBody();
        return response.getRequestCode();
    }
}