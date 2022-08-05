package com.fu.flix.service;

import com.fu.flix.dto.UserNotificationDTO;
import com.fu.flix.dto.request.SaveFCMTokenRequest;
import com.fu.flix.dto.response.SaveFCMTokenResponse;
import org.springframework.http.ResponseEntity;

public interface FCMService {
    String getFCMToken(Long userId);

    ResponseEntity<SaveFCMTokenResponse> saveFCMToken(SaveFCMTokenRequest request);

    void sendAndSaveNotification(UserNotificationDTO userNotificationDTO, String... formatParams);
}
