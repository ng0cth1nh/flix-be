package com.fu.flix.job.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.dao.*;
import com.fu.flix.entity.*;
import com.fu.flix.service.FCMService;
import com.fu.flix.service.ValidatorService;
import com.fu.flix.util.DateFormatUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Transactional
class CronJobImplTest {
    @Mock
    AppConf appConf;
    @Mock
    RepairRequestDAO repairRequestDAO;
    @Mock
    FCMService fcmService;
    @Mock
    RepairerDAO repairerDAO;
    @Mock
    BalanceDAO balanceDAO;
    @Mock
    TransactionHistoryDAO transactionHistoryDAO;
    @Mock
    ValidatorService validatorService;
    @Mock
    RepairRequestMatchingDAO repairRequestMatchingDAO;
    @Mock
    InvoiceDAO invoiceDAO;
    @InjectMocks
    CronJobImpl underTest;
    String NOTIFICATION_DATE_TIME_PATTERN = "HH:mm - dd/MM/yyyy";
    @Mock
    RepairRequest repairRequest;
    @Mock
    List<RepairRequest> repairRequests;
    @Mock
    RepairRequestMatching repairRequestMatching;
    @Mock
    Repairer repairer;
    @Mock
    Balance balance;
    @Mock
    Invoice invoice;

    @BeforeEach
    void setup() {
        repairRequest = new RepairRequest();
        repairRequest.setRequestCode("ABC123456789");
        repairRequest.setExpectStartFixingAt(LocalDateTime.now().plusDays(3L));
        repairRequest.setUserId(36L);

        repairRequests = new ArrayList<>();
        repairRequests.add(repairRequest);

        repairRequestMatching = new RepairRequestMatching();
        repairRequestMatching.setRepairerId(56L);

        repairer = new Repairer();
        repairer.setUserId(56L);

        balance = new Balance();
        balance.setBalance(1000000L);

        invoice = new Invoice();
        invoice.setRequestCode("ABC123456789");
        invoice.setConfirmFixingAt(LocalDateTime.now().plusDays(3L));
    }

    @Test
    void test_run_cancelPendingRequestAutomatically() {
        // given
        Mockito.when(repairRequestDAO.findCancelablePendingRequest()).thenReturn(repairRequests);

        // when
        underTest.cancelPendingRequestAutomatically();
    }

    @Test
    void test_run_cancelApprovalRequestAutomatically() {
        // given
        Mockito.when(repairRequestDAO.findCancelableApprovalRequest()).thenReturn(repairRequests);
        Mockito.when(repairRequestMatchingDAO.findByRequestCode("ABC123456789")).thenReturn(Optional.of(repairRequestMatching));
        Mockito.when(repairerDAO.findByUserId(56L)).thenReturn(Optional.of(repairer));
        Mockito.when(balanceDAO.findByUserId(repairer.getUserId())).thenReturn(Optional.of(balance));

        // when
        underTest.cancelApprovalRequestAutomatically();
    }

    @Test
    void test_run_cancelFixingRequestAutomatically() {
        // given
        Mockito.when(repairRequestDAO.findCancelableFixingRequest()).thenReturn(repairRequests);
        Mockito.when(repairRequestMatchingDAO.findByRequestCode("ABC123456789")).thenReturn(Optional.of(repairRequestMatching));
        Mockito.when(repairerDAO.findByUserId(56L)).thenReturn(Optional.of(repairer));
        Mockito.when(balanceDAO.findByUserId(repairer.getUserId())).thenReturn(Optional.of(balance));

        // when
        underTest.cancelFixingRequestAutomatically();
    }

    @Test
    void test_run_sendNotificationDeadlineFixingAutomatically() {
        // given
        Mockito.when(repairRequestDAO.findRequestToRemindExpectedFixingTimeDeadline()).thenReturn(repairRequests);
        Mockito.when(repairRequestMatchingDAO.findByRequestCode("ABC123456789")).thenReturn(Optional.of(repairRequestMatching));

        // then
        underTest.sendNotificationDeadlineFixingAutomatically();
    }

    @Test
    void test_run_sendNotificationRemindFixingAutomatically() {
        // given
        Mockito.when(repairRequestDAO.findRequestToRemindFixingTask()).thenReturn(repairRequests);
        Mockito.when(repairRequestMatchingDAO.findByRequestCode("ABC123456789")).thenReturn(Optional.of(repairRequestMatching));
        Mockito.when(invoiceDAO.findByRequestCode("ABC123456789")).thenReturn(Optional.of(invoice));

        // when
        underTest.sendNotificationRemindFixingAutomatically();
    }
}