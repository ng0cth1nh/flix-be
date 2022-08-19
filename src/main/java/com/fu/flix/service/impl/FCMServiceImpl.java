package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.Constant;
import com.fu.flix.dao.ImageDAO;
import com.fu.flix.dao.NotificationDAO;
import com.fu.flix.dao.UserDAO;
import com.fu.flix.dto.UserNotificationDTO;
import com.fu.flix.dto.SendNotificationDTO;
import com.fu.flix.dto.request.SaveFCMTokenRequest;
import com.fu.flix.dto.response.SaveFCMTokenResponse;
import com.fu.flix.entity.Image;
import com.fu.flix.entity.User;
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

@Service
@Slf4j
@Transactional
public class FCMServiceImpl implements FCMService {
    private final UserDAO userDAO;
    private final NotificationDAO notificationDAO;
    private final ImageDAO imageDAO;
    private final AppConf appConf;

    public FCMServiceImpl(UserDAO userDAO,
                          NotificationDAO notificationDAO,
                          ImageDAO imageDAO,
                          AppConf appConf) {
        this.userDAO = userDAO;
        this.notificationDAO = notificationDAO;
        this.imageDAO = imageDAO;
        this.appConf = appConf;
    }

    @Override
    public void sendAndSaveNotification(UserNotificationDTO userNotificationDTO, String... formatParams) {
        String title = appConf.getNotification().getTitle().get(userNotificationDTO.getTitleType());
        String message = String.format(appConf.getNotification()
                        .getContent()
                        .get(userNotificationDTO.getMessageStatus()),
                formatParams);

        Image savedImage = saveNotificationImage();
        SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
        sendNotificationDTO.setToken(getFCMToken(userNotificationDTO.getUserId()));
        sendNotificationDTO.setTitle(title);
        sendNotificationDTO.setBody(message);
        sendNotificationDTO.setImageUrl(savedImage.getUrl());
        sendNotificationDTO.setNotificationType(userNotificationDTO.getNotificationType());
        sendNotification(sendNotificationDTO);

        com.fu.flix.entity.Notification notificationData = new com.fu.flix.entity.Notification();
        notificationData.setUserId(userNotificationDTO.getUserId());
        notificationData.setTitle(title);
        notificationData.setContent(message);
        notificationData.setRead(false);
        notificationData.setImageId(savedImage.getId());
        notificationData.setRequestCode(userNotificationDTO.getRequestCode());
        notificationData.setFeedbackId(userNotificationDTO.getFeedbackId());
        notificationData.setType(userNotificationDTO.getNotificationType());
        notificationDAO.save(notificationData);
    }

    private void sendNotification(SendNotificationDTO dto) {
        Notification notification = Notification
                .builder()
                .setTitle(dto.getTitle())
                .setBody(dto.getBody())
                .setImage(dto.getImageUrl())
                .build();

        Message message = Message.builder()
                .setToken(dto.getToken())
                .setNotification(notification)
                .putData("content", dto.getTitle())
                .putData("body", dto.getBody())
                .putData("image", dto.getImageUrl())
                .putData("notificationType", dto.getNotificationType())
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.error("Fail to send firebase notification", e);
        }
    }

    private Image saveNotificationImage() {
        Image image = new Image();
        image.setName(appConf.getNotification().getDefaultImageName());
        image.setUrl(appConf.getNotification().getDefaultImage());
        return imageDAO.save(image);
    }

    @Override
    public String getFCMToken(Long userId) {
        return userDAO.findById(userId).get().getFcmToken();
    }

    @Override
    public ResponseEntity<SaveFCMTokenResponse> saveFCMToken(SaveFCMTokenRequest request) {
        User user = userDAO.findById(request.getUserId()).get();
        user.setFcmToken(request.getToken());
        SaveFCMTokenResponse response = new SaveFCMTokenResponse();
        response.setMessage(Constant.SAVE_FCM_TOKEN_SUCCESS);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
