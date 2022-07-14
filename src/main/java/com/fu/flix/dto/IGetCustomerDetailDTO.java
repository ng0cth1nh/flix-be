package com.fu.flix.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface IGetCustomerDetailDTO {

    String getAvatar();

    String getCustomerName();

    String getCustomerPhone();

    String getStatus();

    LocalDate getDateOfBirth();

    Boolean getGender();

    String getEmail();

    Long getAddressId();

    LocalDateTime getCreatedAt();
}
