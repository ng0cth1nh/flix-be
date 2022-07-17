package com.fu.flix.service;

import com.fu.flix.dto.request.PushNotificationRequest;
import com.fu.flix.dto.request.SaveFCMTokenRequest;
import com.fu.flix.dto.response.PushNotificationResponse;
import com.fu.flix.dto.response.SaveFCMTokenResponse;
import com.fu.flix.entity.Notification;
import org.springframework.http.ResponseEntity;

public interface FCMService {
    ResponseEntity<PushNotificationResponse> sendPnsToDevice(PushNotificationRequest notificationRequest);
    String getFCMToken (Long userId);
    void saveNotification(Notification notification);
    ResponseEntity<SaveFCMTokenResponse> saveFCMToken(SaveFCMTokenRequest request);
}
