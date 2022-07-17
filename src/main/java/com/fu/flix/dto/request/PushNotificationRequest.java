package com.fu.flix.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PushNotificationRequest extends DataRequest {
    private String token;
    private String title;
    private String body;
    private String imageUrl;
}
