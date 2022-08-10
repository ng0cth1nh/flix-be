package com.fu.flix.job;

import com.fu.flix.entity.RepairRequest;
import com.fu.flix.entity.UserVoucher;

import java.time.LocalDateTime;
import java.util.Collection;

public interface CronJob {
    boolean isOnRequestCancelTime(LocalDateTime expectStartFixingAt);

    void cancelPendingRequestAutomatically();

    void cancelApprovalRequestAutomatically();

    void cancelFixingRequestAutomatically();

    void monetaryFine(Long repairerId, String requestCode);

    void updateRepairerAfterCancelFixingRequest(String requestCode);

    void updateRequestAfterCancel(String reason, String roleId, RepairRequest repairRequest);

    void refundVoucher(RepairRequest repairRequest);

    UserVoucher getUserVoucher(Collection<UserVoucher> userVouchers, Long voucherId);

    void sendNotificationDeadlineFixingAutomatically();

    void sendNotificationRemindFixingAutomatically();
}
