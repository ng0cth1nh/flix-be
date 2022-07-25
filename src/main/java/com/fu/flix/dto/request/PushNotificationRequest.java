package com.fu.flix.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PushNotificationRequest extends DataRequest {
    private String token;
    private String title;
    private String body;
    private MultipartFile image;
}
