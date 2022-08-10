package com.fu.flix.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface IRepairerDetailDTO {
    Long getId();

    String getAvatar();

    String getRepairerName();

    String getRepairerPhone();

    String getStatus();

    LocalDate getDateOfBirth();

    Boolean getGender();

    String getEmail();

    Long getAddressId();

    LocalDateTime getCreatedAt();

    Integer getExperienceYear();

    String getExperienceDescription();

    String getIdentityCardNumber();

    String getIdentityCardType();

    String getFrontImage();

    String getBackSideImage();

    LocalDateTime getAcceptedAccountAt();

    String getCvStatus();
}
