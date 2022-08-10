package com.fu.flix.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserNotificationDTO {
    private String titleType;
    private String messageStatus;
    private Long userId;
    private String notificationType;
    private Long feedbackId;
    private String requestCode;
}
