package com.fu.flix.service;

import com.fu.flix.dto.request.PushNotificationRequest;
import com.fu.flix.dto.request.SaveFCMTokenRequest;
import com.fu.flix.dto.response.PushNotificationResponse;
import com.fu.flix.dto.response.SaveFCMTokenResponse;
import com.fu.flix.entity.Notification;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface FCMService {
    ResponseEntity<PushNotificationResponse> sendPnsToDevice(PushNotificationRequest notificationRequest) throws IOException;
    String getFCMToken (Long userId);
    ResponseEntity<SaveFCMTokenResponse> saveFCMToken(SaveFCMTokenRequest request);

    void sendNotification(String titleType, String messageType, Long userId, String... formatParams) throws IOException;
}
