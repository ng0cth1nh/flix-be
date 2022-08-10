package com.fu.flix.controller;


import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.service.FCMService;
import com.fu.flix.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@Slf4j
@RequestMapping("api/v1/user")
public class UserController {
    private final UserService userService;
    private final FCMService fcmService;

    public UserController(UserService userService,
                          FCMService fcmService) {
        this.userService = userService;
        this.fcmService = fcmService;
    }

    @PutMapping("avatar")
    public ResponseEntity<UpdateAvatarResponse> updateAvatar(UpdateAvatarRequest request) throws IOException {
        return userService.updateAvatar(request);
    }

    @GetMapping("notifications")
    public ResponseEntity<NotificationResponse> getNotifications(NotificationRequest request) {
        return userService.getNotifications(request);
    }

    @PutMapping("changePassword")
    public ResponseEntity<ChangePasswordResponse> changePassword(@RequestBody ChangePasswordRequest request) {
        return userService.changePassword(request);
    }

    @PostMapping("feedback")
    public ResponseEntity<UserCreateFeedbackResponse> createFeedback(UserCreateFeedbackRequest request) throws IOException {
        return userService.createFeedback(request);
    }

    @PostMapping("saveFCMToken")
    public ResponseEntity<SaveFCMTokenResponse> saveFCMToken(@RequestBody SaveFCMTokenRequest request) {
        return fcmService.saveFCMToken(request);
    }

    @GetMapping("information")
    public ResponseEntity<UserInfoResponse> getUserInfo(UserInfoRequest request) {
        return userService.getUserInfo(request);
    }

    @DeleteMapping("notification")
    public ResponseEntity<DeleteNotificationResponse> deleteNotification(DeleteNotificationRequest request) {
        return userService.deleteNotification(request);
    }

    @PutMapping("notification")
    public ResponseEntity<PutNotificationResponse> markReadNotification(@RequestBody PutNotificationRequest request) {
        return userService.markReadNotification(request);
    }
}
