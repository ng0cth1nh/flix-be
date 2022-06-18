package com.fu.flix.service.impl;


import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.Constant;
import com.fu.flix.dao.ImageDAO;
import com.fu.flix.dao.NotificationDAO;
import com.fu.flix.dao.UserDAO;
import com.fu.flix.dto.NotificationDTO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.ChangePasswordRequest;
import com.fu.flix.dto.request.NotificationRequest;
import com.fu.flix.dto.request.ResetPasswordRequest;
import com.fu.flix.dto.request.UpdateAvatarRequest;
import com.fu.flix.dto.response.ChangePasswordResponse;
import com.fu.flix.dto.response.NotificationResponse;
import com.fu.flix.dto.response.ResetPasswordResponse;
import com.fu.flix.dto.response.UpdateAvatarResponse;
import com.fu.flix.entity.Image;
import com.fu.flix.entity.Notification;
import com.fu.flix.entity.User;
import com.fu.flix.service.CloudStorageService;
import com.fu.flix.service.UserService;
import com.fu.flix.util.DateFormatUtil;
import com.fu.flix.util.InputValidation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.fu.flix.constant.Constant.*;

@Service
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
    private final ImageDAO imageDAO;
    private final CloudStorageService cloudStorageService;
    private final UserDAO userDAO;
    private final AppConf appConf;
    private final NotificationDAO notificationDAO;
    private final PasswordEncoder passwordEncoder;
    private final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public UserServiceImpl(ImageDAO imageDAO,
                           CloudStorageService cloudStorageService,
                           UserDAO userDAO,
                           AppConf appConf,
                           NotificationDAO notificationDAO,
                           PasswordEncoder passwordEncoder) {
        this.imageDAO = imageDAO;
        this.cloudStorageService = cloudStorageService;
        this.userDAO = userDAO;
        this.appConf = appConf;
        this.notificationDAO = notificationDAO;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ResponseEntity<UpdateAvatarResponse> updateAvatar(UpdateAvatarRequest request) throws IOException {
        MultipartFile avatar = request.getAvatar();
        User user = userDAO.findByUsername(request.getUsername()).get();
        Image oldImage = imageDAO.findById(user.getAvatar()).get();

        if (avatar != null) {
            String url = cloudStorageService.uploadImage(avatar);

            if (isDefaultAvatar(oldImage)) {
                user = addNewAvatarToUser(user, url);
                userDAO.save(user);
            } else {
                oldImage.setUrl(url);
            }
        }

        UpdateAvatarResponse response = new UpdateAvatarResponse();
        response.setMessage(Constant.UPDATE_AVATAR_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public User addNewAvatarToUser(User user, String url) {
        Image newImage = new Image();
        newImage.setName(user.getFullName());
        newImage.setUrl(url);
        Image savedImage = imageDAO.save(newImage);
        user.setAvatar(savedImage.getId());
        return user;
    }

    private boolean isDefaultAvatar(Image image) {
        return image.getId().equals(appConf.getDefaultAvatar());
    }

    @Override
    public ResponseEntity<NotificationResponse> getNotifications(NotificationRequest request) {
        User user = userDAO.findByUsername(request.getUsername()).get();
        List<Notification> notifications = notificationDAO.findByUserIdAndDeletedAtIsNull(user.getId());

        List<NotificationDTO> notificationDTOS = notifications.stream()
                .map(notification -> {
                    NotificationDTO dto = new NotificationDTO();
                    Long imageId = notification.getImageId();

                    if (imageId != null) {
                        Image image = imageDAO.findById(imageId).get();
                        dto.setImageUrl(image.getUrl());
                    }

                    dto.setTitle(notification.getTitle());
                    dto.setContent(notification.getContent());
                    dto.setRead(notification.isRead());
                    dto.setDate(DateFormatUtil.toString(notification.getDate(), DATE_TIME_PATTERN));

                    return dto;
                }).collect(Collectors.toList());

        NotificationResponse response = new NotificationResponse();
        response.setNotifications(notificationDTOS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ChangePasswordResponse> changePassword(ChangePasswordRequest request) {
        User user = userDAO.findByUsername(request.getUsername()).get();
        String oldPassword = request.getOldPassword();
        boolean matches = passwordEncoder.matches(oldPassword, user.getPassword());
        if (!matches) {
            throw new GeneralException(WRONG_PASSWORD);
        }

        String newPassword = request.getNewPassword();
        if (!InputValidation.isPasswordValid(newPassword)) {
            throw new GeneralException(INVALID_PASSWORD);
        }

        if (oldPassword.equals(newPassword)) {
            throw new GeneralException(NEW_PASSWORD_MUST_BE_DIFFERENT_FROM_OLD_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        ChangePasswordResponse response = new ChangePasswordResponse();
        response.setMessage(Constant.CHANGE_PASSWORD_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ResetPasswordResponse> resetPassword(ResetPasswordRequest request) {
        User user = userDAO.findByUsername(request.getUsername()).get();

        String newPassword = request.getNewPassword();
        if (!InputValidation.isPasswordValid(newPassword)) {
            throw new GeneralException(INVALID_PASSWORD);
        }
        user.setPassword(passwordEncoder.encode(newPassword));

        ResetPasswordResponse response = new ResetPasswordResponse();
        response.setMessage(RESET_PASSWORD_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
