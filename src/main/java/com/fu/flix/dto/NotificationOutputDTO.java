package com.fu.flix.dto;

import lombok.Data;

@Data
public class NotificationOutputDTO {
    private String title;
    private String content;
    private String imageUrl;
    private boolean isRead;
    private String date;
    private String type;
    private Long id;
    private String requestCode;
    private Long feedbackId;
}
