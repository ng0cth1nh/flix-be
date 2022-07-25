package com.fu.flix.dto;

import java.time.LocalDateTime;

public interface IBanUserDTO {
    Long getId();

    String getAvatar();

    String getName();

    String getPhone();

    String getRole();

    String getBanReason();

    LocalDateTime getBanAt();
}
