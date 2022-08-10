package com.fu.flix.dto.response;

import com.fu.flix.dto.NotificationOutputDTO;
import lombok.Data;

import java.util.List;

@Data
public class NotificationResponse {
    private List<NotificationOutputDTO> notifications;
    private long totalRecord;
    private long numberOfUnread;
}
