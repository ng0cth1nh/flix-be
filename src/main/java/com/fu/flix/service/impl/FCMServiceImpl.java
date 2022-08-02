package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.Constant;
import com.fu.flix.dao.ImageDAO;
import com.fu.flix.dao.NotificationDAO;
import com.fu.flix.dao.UserDAO;
import com.fu.flix.dto.request.PushNotificationRequest;
import com.fu.flix.dto.request.SaveFCMTokenRequest;
import com.fu.flix.dto.response.PushNotificationResponse;
import com.fu.flix.dto.response.SaveFCMTokenResponse;
import com.fu.flix.entity.Image;
import com.fu.flix.entity.User;
import com.fu.flix.service.CloudStorageService;
import com.fu.flix.service.FCMService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;

@Service
@Slf4j
@Transactional
public class FCMServiceImpl implements FCMService {
    private final UserDAO userDAO;
    private final NotificationDAO notificationDAO;
    private final ImageDAO imageDAO;
    private final CloudStorageService cloudStorageService;
    private final AppConf appConf;

    public FCMServiceImpl(UserDAO userDAO, NotificationDAO notificationDAO, ImageDAO imageDAO, CloudStorageService cloudStorageService, AppConf appConf) {
        this.userDAO = userDAO;
        this.notificationDAO = notificationDAO;
        this.imageDAO = imageDAO;
        this.cloudStorageService = cloudStorageService;
        this.appConf = appConf;
    }

    @Override
    public ResponseEntity<PushNotificationResponse> sendPnsToDevice(PushNotificationRequest notificationRequest) throws IOException {
        Image savedImage = null;
        if (notificationRequest.getImage() != null) {
            Image image = new Image();
            image.setName("notification image");
            String imageUrl = cloudStorageService.uploadImage(notificationRequest.getImage());
            image.setUrl(imageUrl);
            savedImage = imageDAO.save(image);
        }

        Notification notification = Notification
                .builder()
                .setTitle(notificationRequest.getTitle())
                .setBody(notificationRequest.getBody())
                .setImage(savedImage == null ? appConf.getNotification().getDefaultImage() : savedImage.getUrl())
                .build();

        Message message = Message.builder()
                .setToken(notificationRequest.getToken())
                .setNotification(notification)
                .putData("content", notificationRequest.getTitle())
                .putData("body", notificationRequest.getBody())
                .putData("image", savedImage == null ? appConf.getNotification().getDefaultImage() : savedImage.getUrl())
                .build();
        PushNotificationResponse response = new PushNotificationResponse();
        try {
            FirebaseMessaging.getInstance().send(message);
            response.setMessage(Constant.PUSH_NOTIFICATION_SUCCESS);

            com.fu.flix.entity.Notification noti = new com.fu.flix.entity.Notification();
            noti.setUserId(notificationRequest.getUserId());
            noti.setTitle(notificationRequest.getTitle());
            noti.setContent(notificationRequest.getBody());
            noti.setRead(false);
            if(savedImage != null) noti.setImageId(savedImage.getId());
            saveNotification(noti);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (FirebaseMessagingException e) {
            log.error("Fail to send firebase notification", e);
            response.setMessage(Constant.PUSH_NOTIFICATION_FAIL);
            return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
        }
    }

    @Override
    public String getFCMToken(Long userId) {
        return userDAO.findById(userId).get().getFcmToken();
    }

    @Override
    public void saveNotification(com.fu.flix.entity.Notification notification) {
        notificationDAO.save(notification);
    }

    @Override
    public ResponseEntity<SaveFCMTokenResponse> saveFCMToken(SaveFCMTokenRequest request) {
        User user = userDAO.findById(request.getUserId()).get();
        user.setFcmToken(request.getToken());
        userDAO.save(user);
        SaveFCMTokenResponse response = new SaveFCMTokenResponse();
        response.setMessage(Constant.SAVE_FCM_TOKEN_SUCCESS);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
