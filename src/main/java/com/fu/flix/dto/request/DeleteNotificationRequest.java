package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteNotificationRequest extends DataRequest {
    private Long id;
}
