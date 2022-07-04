package com.fu.flix.dto;

import java.time.LocalDateTime;

public interface IRequestingDTO {
    String getCustomerName();

    String getAvatar();

    String getServiceName();

    LocalDateTime getExpectFixingTime();

    Long getAddressId();

    String getDescription();

    String getRequestCode();

    String getIconImage();

    String getCreatedAt();
}
