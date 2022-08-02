package com.fu.flix.dto;

import java.time.LocalDate;

public interface IRepairerProfileDTO {
    String getFullName();

    String getAvatar();

    String getPhone();

    LocalDate getDateOfBirth();

    Boolean getGender();

    String getEmail();

    String getExperienceDescription();

    String getIdentityCardNumber();

    Long getAddressId();

    Long getBalance();

    String getRole();

    String getCityId();

    String getDistrictId();

    String getCommuneId();
}
