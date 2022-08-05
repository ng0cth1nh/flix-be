package com.fu.flix.service;

import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.entity.User;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface UserService {
    ResponseEntity<UpdateAvatarResponse> updateAvatar(UpdateAvatarRequest request) throws IOException;

    User addNewAvatarToUser(User user, String url);

    ResponseEntity<NotificationResponse> getNotifications(NotificationRequest request);

    ResponseEntity<ChangePasswordResponse> changePassword(ChangePasswordRequest request);

    ResponseEntity<UserCreateFeedbackResponse> createFeedback(UserCreateFeedbackRequest request) throws IOException;

    ResponseEntity<UserInfoResponse> getUserInfo(UserInfoRequest request);

    ResponseEntity<DeleteNotificationResponse> deleteNotification(DeleteNotificationRequest request);
}
