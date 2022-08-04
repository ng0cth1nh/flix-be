package com.fu.flix.dto;

import lombok.Data;

@Data
public class NotificationDTO {
    private String title;
    private String content;
    private String imageUrl;
    private boolean isRead;
    private String date;
    private String type;
}
