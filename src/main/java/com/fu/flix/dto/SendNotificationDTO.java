package com.fu.flix.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendNotificationDTO {
    private String token;
    private String title;
    private String body;
    private String imageUrl;
    private String notificationType;
}
