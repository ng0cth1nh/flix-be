package com.fu.flix.dto;

public interface IAddressDTO {
    Long getAddressId();

    String getAddressName();

    String getCustomerName();

    String getCustomerPhone();

    Boolean getIsMainAddress();

    String getDistrictId();

    String getCityId();

    String getCommuneId();
}
