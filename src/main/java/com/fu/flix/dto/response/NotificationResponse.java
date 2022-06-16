package com.fu.flix.dto.response;

import com.fu.flix.dto.NotificationDTO;
import lombok.Data;

import java.util.List;

@Data
public class NotificationResponse {
    private List<NotificationDTO> notifications;
}
