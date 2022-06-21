package com.fu.flix.service.impl;


import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.Constant;
import com.fu.flix.constant.enums.CommentType;
import com.fu.flix.constant.enums.FeedbackType;
import com.fu.flix.constant.enums.RoleType;
import com.fu.flix.constant.enums.Status;
import com.fu.flix.dao.*;
import com.fu.flix.dto.NotificationDTO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.entity.*;
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fu.flix.constant.Constant.*;
import static com.fu.flix.constant.enums.RoleType.*;

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
    private final FeedbackDAO feedbackDAO;
    private final RepairRequestDAO repairRequestDAO;
    private final CommentDAO commentDAO;
    private final InvoiceDAO invoiceDAO;
    private final RepairRequestMatchingDAO repairRequestMatchingDAO;
    private final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public UserServiceImpl(ImageDAO imageDAO,
                           CloudStorageService cloudStorageService,
                           UserDAO userDAO,
                           AppConf appConf,
                           NotificationDAO notificationDAO,
                           PasswordEncoder passwordEncoder,
                           FeedbackDAO feedbackDAO,
                           RepairRequestDAO repairRequestDAO,
                           CommentDAO commentDAO,
                           InvoiceDAO invoiceDAO,
                           RepairRequestMatchingDAO repairRequestMatchingDAO) {
        this.imageDAO = imageDAO;
        this.cloudStorageService = cloudStorageService;
        this.userDAO = userDAO;
        this.appConf = appConf;
        this.notificationDAO = notificationDAO;
        this.passwordEncoder = passwordEncoder;
        this.feedbackDAO = feedbackDAO;
        this.repairRequestDAO = repairRequestDAO;
        this.commentDAO = commentDAO;
        this.invoiceDAO = invoiceDAO;
        this.repairRequestMatchingDAO = repairRequestMatchingDAO;
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

    @Override
    public ResponseEntity<FeedbackResponse> createFeedback(FeedbackRequest request) throws IOException {
        String requestCode = request.getRequestCode();
        User user = userDAO.findByUsername(request.getUsername()).get();
        Feedback feedback = new Feedback();
        feedback.setCreatedById(user.getId());
        feedback.setTitle(request.getTitle());
        feedback.setDescription(request.getDescription());
        feedback.setStatusId(Status.PENDING.getId());

        try {
            feedback.setType(FeedbackType.valueOf(request.getFeedbackType()).name());
        } catch (Exception e) {
            throw new GeneralException(INVALID_FEEDBACK_TYPE);
        }

        if (requestCode != null && repairRequestDAO.findByRequestCode(requestCode).isEmpty()) {
            throw new GeneralException(INVALID_REQUEST_CODE);
        }

        feedback.setRequestCode(requestCode);

        for (MultipartFile multipartFile : request.getImages()) {
            String url = cloudStorageService.uploadImage(multipartFile);
            Image image = new Image();
            image.setName(request.getTitle());
            image.setUrl(url);
            Image savedImage = imageDAO.save(image);

            feedback.getImages().add(savedImage);
        }

        feedbackDAO.save(feedback);

        FeedbackResponse response = new FeedbackResponse();
        response.setMessage(CREATE_FEEDBACK_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CommentResponse> createComment(CommentRequest request) {
        String requestCode = request.getRequestCode();

        if (requestCode == null) {
            throw new GeneralException(INVALID_REQUEST_CODE);
        }

        Optional<Invoice> optionalInvoice = invoiceDAO.findByRequestCode(requestCode);
        if (optionalInvoice.isEmpty()) {
            throw new GeneralException(INVALID_REQUEST_CODE);
        }

        Invoice invoice = optionalInvoice.get();
        if (!Status.DONE.getId().equals(invoice.getStatusId())) {
            throw new GeneralException(CAN_NOT_COMMENT_WHEN_STATUS_NOT_DONE);
        }

        User user = userDAO.findByUsername(request.getUsername()).get();
        String commentType = getCommentType(user.getRoles());
        Optional<Comment> optionalComment = commentDAO.findComment(requestCode, commentType);
        if (optionalComment.isPresent()) {
            throw new GeneralException(COMMENT_EXISTED);
        }

        RepairRequest repairRequest = repairRequestDAO.findByRequestCode(requestCode).get();
        RepairRequestMatching repairRequestMatching = repairRequestMatchingDAO.findByRequestCode(requestCode).get();

        Comment comment = new Comment();
        comment.setRating(getRatingValidated(request.getRating()));
        comment.setComment(request.getComment());
        comment.setRequestCode(requestCode);
        comment.setRepairerId(repairRequestMatching.getRepairerId());
        comment.setCustomerId(repairRequest.getUserId());
        comment.setType(commentType);

        commentDAO.save(comment);

        CommentResponse response = new CommentResponse();
        response.setMessage(COMMENT_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String getCommentType(Collection<Role> roles) {
        for (Role role : roles) {
            RoleType roleType = valueOf( role.getName());
            if (ROLE_CUSTOMER.equals(roleType)) {
                return CommentType.CUSTOMER_COMMENT.name();
            } else if (ROLE_REPAIRER.equals(roleType)) {
                return CommentType.REPAIRER_COMMENT.name();
            }
        }
        return null;
    }


    private Integer getRatingValidated(Integer rating) {
        if (rating == null) {
            throw new GeneralException(RATING_IS_REQUIRED);
        }
        if (rating > 5 || rating < 1) {
            throw new GeneralException(RATING_MUST_IN_RANGE_1_TO_5);
        }
        return rating;
    }
}
