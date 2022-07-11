package com.fu.flix.dto;

import java.time.LocalDateTime;

public interface IDetailFixingRequestForRepairerDTO {
    String getStatus();

    String getServiceImage();

    Long getServiceId();

    String getServiceName();

    Long getCustomerId();

    String getAvatar();

    Long getAddressId();

    String getCustomerPhone();

    String getCustomerName();

    LocalDateTime getExpectFixingTime();

    String getRequestDescription();

    Long getVoucherId();

    String getPaymentMethod();

    LocalDateTime getCreatedAt();

    Long getTotalPrice();

    Long getActualPrice();

    Long getVatPrice();

    String getRequestCode();

    Long getInspectionPrice();

    Long getTotalDiscount();

    LocalDateTime getApprovedTime();
}
