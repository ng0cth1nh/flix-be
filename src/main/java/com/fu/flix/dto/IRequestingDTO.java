package com.fu.flix.dto;

import java.time.LocalDateTime;

public interface IRequestingDTO {
    String getCustomerName();

    String getAvatar();

    String getServiceName();

    LocalDateTime getExpectFixingTime();

    String getAddress();

    String getDescription();

    String getRequestCode();

    String getIconImage();

    String getCreatedAt();
}
