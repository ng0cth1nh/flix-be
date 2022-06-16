package com.fu.flix.service;

import com.fu.flix.dto.request.ChangePasswordRequest;
import com.fu.flix.dto.request.NotificationRequest;
import com.fu.flix.dto.request.UpdateAvatarRequest;
import com.fu.flix.dto.response.ChangePasswordResponse;
import com.fu.flix.dto.response.NotificationResponse;
import com.fu.flix.dto.response.UpdateAvatarResponse;
import com.fu.flix.entity.User;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface UserService {
    ResponseEntity<UpdateAvatarResponse> updateAvatar(UpdateAvatarRequest request) throws IOException;

    User addNewAvatarToUser(User user, String url);

    ResponseEntity<NotificationResponse> getNotifications(NotificationRequest request);
    ResponseEntity<ChangePasswordResponse> changePassword(ChangePasswordRequest request);
}
