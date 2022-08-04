package com.fu.flix.dto;

import java.time.LocalDateTime;

public interface IRepairerCommentDTO {
    Long getCustomerId();

    String getCustomerName();

    Integer getRating();

    String getComment();

    LocalDateTime getCreatedAt();

    String getCustomerAvatar();
}
