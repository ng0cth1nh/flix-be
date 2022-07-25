package com.fu.flix.dto;

import java.time.LocalDateTime;

public interface IPendingRepairerDTO {
    Long getId();

    String getRepairerName();

    String getRepairerPhone();

    LocalDateTime getCreatedAt();
}
