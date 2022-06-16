package com.fu.flix.controller;


import com.fu.flix.dto.request.ChangePasswordRequest;
import com.fu.flix.dto.request.NotificationRequest;
import com.fu.flix.dto.request.UpdateAvatarRequest;
import com.fu.flix.dto.response.ChangePasswordResponse;
import com.fu.flix.dto.response.NotificationResponse;
import com.fu.flix.dto.response.UpdateAvatarResponse;
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

    public UserController(UserService userService) {
        this.userService = userService;
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
}
