package com.fu.flix.job.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.enums.NotificationStatus;
import com.fu.flix.constant.enums.NotificationType;
import com.fu.flix.dao.*;
import com.fu.flix.dto.UserNotificationDTO;
import com.fu.flix.entity.*;
import com.fu.flix.job.CronJob;
import com.fu.flix.service.FCMService;
import com.fu.flix.service.ValidatorService;
import com.fu.flix.util.DateFormatUtil;
import com.fu.flix.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static com.fu.flix.constant.Constant.*;
import static com.fu.flix.constant.enums.RequestStatus.CANCELLED;
import static com.fu.flix.constant.enums.RoleType.ROLE_MANAGER;
import static com.fu.flix.constant.enums.TransactionStatus.SUCCESS;
import static com.fu.flix.constant.enums.TransactionType.FINED;

@Service
@Slf4j
@Transactional
public class CronJobImpl implements CronJob {
    private final AppConf appConf;
    private final RepairRequestDAO repairRequestDAO;
    private final FCMService fcmService;
    private final RepairerDAO repairerDAO;
    private final BalanceDAO balanceDAO;
    private final TransactionHistoryDAO transactionHistoryDAO;
    private final ValidatorService validatorService;
    private final RepairRequestMatchingDAO repairRequestMatchingDAO;
    private final InvoiceDAO invoiceDAO;
    private final String NOTIFICATION_DATE_TIME_PATTERN = "HH:mm - dd/MM/yyyy";
    public CronJobImpl(AppConf appConf,
                       RepairRequestDAO repairRequestDAO,
                       FCMService fcmService,
                       RepairerDAO repairerDAO,
                       BalanceDAO balanceDAO,
                       TransactionHistoryDAO transactionHistoryDAO,
                       ValidatorService validatorService,
                       RepairRequestMatchingDAO repairRequestMatchingDAO,
                       InvoiceDAO invoiceDAO) {
        this.appConf = appConf;
        this.repairRequestDAO = repairRequestDAO;
        this.fcmService = fcmService;
        this.repairerDAO = repairerDAO;
        this.balanceDAO = balanceDAO;
        this.transactionHistoryDAO = transactionHistoryDAO;
        this.validatorService = validatorService;
        this.repairRequestMatchingDAO = repairRequestMatchingDAO;
        this.invoiceDAO = invoiceDAO;
    }

    // job run every 5 minute
    @Override
    @Scheduled(cron = "0 0/5 * * * ?")
    public void cancelPendingRequestAutomatically() {
        log.info("Start cancel pending request automatically at: " + LocalDateTime.now());
        List<RepairRequest> repairRequests = repairRequestDAO.findCancelablePendingRequest();
        repairRequests.parallelStream()
                .forEach(repairRequest -> {
                    refundVoucher(repairRequest);
                    updateRequestAfterCancel(SYSTEM_CANCEL_PENDING_REQUEST, ROLE_MANAGER.getId(), repairRequest);

                    String requestCode = repairRequest.getRequestCode();
                    UserNotificationDTO customerNotificationDTO = new UserNotificationDTO(
                            "request",
                            NotificationStatus.REQUEST_CANCELED_AUTOMATICALLY.name(),
                            repairRequest.getUserId(),
                            NotificationType.REQUEST_CANCELED.name(),
                            null,
                            requestCode
                    );
                    fcmService.sendAndSaveNotification(customerNotificationDTO, requestCode);
                });
    }

    // job run every 5 minutes
    @Override
    @Scheduled(cron = "0 0/5 * * * ?")
    public void cancelApprovalRequestAutomatically() {
        List<RepairRequest> repairRequests = repairRequestDAO.findCancelableApprovalRequest();
        repairRequests.parallelStream()
                .forEach(repairRequest -> {
                    String requestCode = repairRequest.getRequestCode();
                    RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();
                    Long repairerId = repairRequestMatching.getRepairerId();

                    monetaryFine(repairerId, requestCode);
                    refundVoucher(repairRequest);
                    updateRequestAfterCancel(SYSTEM_CANCEL_APPROVAL_REQUEST, ROLE_MANAGER.getId(), repairRequest);

                    UserNotificationDTO customerNotificationDTO = new UserNotificationDTO(
                            "request",
                            NotificationStatus.REQUEST_CANCELED_AUTOMATICALLY.name(),
                            repairRequest.getUserId(),
                            NotificationType.REQUEST_CANCELED.name(),
                            null,
                            requestCode
                    );

                    UserNotificationDTO repairerNotificationDTO = new UserNotificationDTO(
                            "request",
                            NotificationStatus.REQUEST_CANCELED_AUTOMATICALLY.name(),
                            repairerId,
                            NotificationType.REQUEST_CANCELED.name(),
                            null,
                            requestCode
                    );

                    fcmService.sendAndSaveNotification(customerNotificationDTO, requestCode);
                    fcmService.sendAndSaveNotification(repairerNotificationDTO, requestCode);
                });
    }

    // job run every 5 minutes
    @Override
    @Scheduled(cron = "0 0/5 * * * ?")
    public void cancelFixingRequestAutomatically() {
        List<RepairRequest> repairRequests = repairRequestDAO.findCancelableFixingRequest();
        repairRequests.parallelStream()
                .forEach(repairRequest -> {
                    String requestCode = repairRequest.getRequestCode();
                    RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();
                    Long repairerId = repairRequestMatching.getRepairerId();

                    monetaryFine(repairerId, requestCode);
                    refundVoucher(repairRequest);
                    updateRequestAfterCancel(SYSTEM_CANCEL_FIXING_REQUEST, ROLE_MANAGER.getId(), repairRequest);
                    updateRepairerAfterCancelFixingRequest(requestCode);

                    UserNotificationDTO customerNotificationDTO = new UserNotificationDTO(
                            "request",
                            NotificationStatus.REQUEST_CANCELED_AUTOMATICALLY.name(),
                            repairRequest.getUserId(),
                            NotificationType.REQUEST_CANCELED.name(),
                            null,
                            requestCode
                    );

                    UserNotificationDTO repairerNotificationDTO = new UserNotificationDTO(
                            "request",
                            NotificationStatus.REQUEST_CANCELED_AUTOMATICALLY.name(),
                            repairerId,
                            NotificationType.REQUEST_CANCELED.name(),
                            null,
                            requestCode
                    );

                    fcmService.sendAndSaveNotification(customerNotificationDTO, requestCode);
                    fcmService.sendAndSaveNotification(repairerNotificationDTO, requestCode);
                });
    }

    // job run every 1 hour
    @Override
    @Scheduled(cron = "0 0 */1 * * *")
    public void sendNotificationDeadlineFixingAutomatically() {
        log.info("Start end notification remind at: " + LocalDateTime.now());
        List<RepairRequest> repairRequests = repairRequestDAO.findRequestToRemindExpectedFixingTimeDeadline();
        repairRequests.parallelStream()
                .forEach(repairRequest -> {
                    String requestCode = repairRequest.getRequestCode();
                    RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();

                    UserNotificationDTO repairerNotificationDTO = new UserNotificationDTO(
                            "request",
                            NotificationStatus.REMIND_EXPECT_FIXING_TIME_DEADLINE.name(),
                            repairRequestMatching.getRepairerId(),
                            NotificationType.REMIND.name(),
                            null,
                            requestCode);

                    fcmService.sendAndSaveNotification(repairerNotificationDTO,
                            requestCode,
                            DateFormatUtil.toString(repairRequest.getExpectStartFixingAt(), NOTIFICATION_DATE_TIME_PATTERN));
                });
    }

    // job run every 7AM
    @Override
    @Scheduled(cron = "0 0 7 * * *")
    public void sendNotificationRemindFixingAutomatically() {
        log.info("Start end notification remind at: " + LocalDateTime.now());
        List<RepairRequest> repairRequests = repairRequestDAO.findRequestToRemindFixingTask();
        repairRequests.parallelStream()
                .forEach(repairRequest -> {
                    String requestCode = repairRequest.getRequestCode();
                    RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();
                    Invoice invoice = invoiceDAO.findByRequestCode(requestCode).get();

                    UserNotificationDTO repairerNotificationDTO = new UserNotificationDTO(
                            "request",
                            NotificationStatus.REMIND_FIXING_TASK.name(),
                            repairRequestMatching.getRepairerId(),
                            NotificationType.REMIND.name(),
                            null,
                            requestCode);

                    fcmService.sendAndSaveNotification(repairerNotificationDTO,
                            requestCode,
                            DateFormatUtil.toString(invoice.getConfirmFixingAt().plusSeconds(appConf.getCancelableFixingRequestInterval()),
                                    NOTIFICATION_DATE_TIME_PATTERN));
                });
    }

    @Override
    public void refundVoucher(RepairRequest repairRequest) {
        Long voucherId = repairRequest.getVoucherId();
        if (voucherId != null) {
            User user = validatorService.getUserValidated(repairRequest.getUserId());
            Collection<UserVoucher> userVouchers = user.getUserVouchers();
            UserVoucher userVoucher = getUserVoucher(userVouchers, voucherId);
            userVoucher.setQuantity(userVoucher.getQuantity() + 1);
        }
    }

    @Override
    public UserVoucher getUserVoucher(Collection<UserVoucher> userVouchers, Long voucherId) {
        return userVouchers.stream()
                .filter(uv -> uv.getUserVoucherId().getVoucherId().equals(voucherId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void updateRequestAfterCancel(String reason, String roleId, RepairRequest repairRequest) {
        repairRequest.setStatusId(CANCELLED.getId());
        repairRequest.setCancelledByRoleId(roleId);
        repairRequest.setReasonCancel(reason);
    }

    @Override
    public boolean isOnRequestCancelTime(LocalDateTime expectStartFixingAt) {
        Duration duration = Duration.between(LocalDateTime.now(), expectStartFixingAt);
        return duration.getSeconds() >= 0 && duration.getSeconds() < this.appConf.getCancelablePendingRequestInterval();
    }

    @Override
    public void monetaryFine(Long repairerId, String requestCode) {
        Long fineMoney = this.appConf.getFine();
        Repairer repairer = repairerDAO.findByUserId(repairerId).get();
        Balance balance = balanceDAO.findByUserId(repairer.getUserId()).get();

        balance.setBalance(balance.getBalance() - fineMoney);

        TransactionHistory finedTransaction = new TransactionHistory();
        finedTransaction.setUserId(repairerId);
        finedTransaction.setAmount(fineMoney);
        finedTransaction.setType(FINED.name());
        finedTransaction.setRequestCode(requestCode);
        finedTransaction.setStatus(SUCCESS.name());
        finedTransaction.setTransactionCode(RandomUtil.generateCode());
        transactionHistoryDAO.save(finedTransaction);
    }

    @Override
    public void updateRepairerAfterCancelFixingRequest(String requestCode) {
        RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();
        Repairer repairer = repairerDAO.findByUserId(repairRequestMatching.getRepairerId()).get();
        repairer.setRepairing(false);
    }
}
