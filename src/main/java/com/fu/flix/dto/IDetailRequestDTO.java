package com.fu.flix.dto;

import java.time.LocalDateTime;

public interface IDetailRequestDTO {
    String getRequestCode();

    String getCustomerName();

    String getCustomerPhone();

    String getRepairerName();

    String getRepairerPhone();

    String getStatus();

    String getCustomerAddress();

    String getDescription();

    String getServiceName();

    Long getVoucherId();

    LocalDateTime getExpectedFixingTime();

    String getPaymentMethod();

    String getCancelReason();

    LocalDateTime getCreatedAt();

    Long getTotalPrice();

    Long getVatPrice();

    Long getActualPrice();

    Long getTotalDiscount();

    Long getInspectionPrice();

    Long getTotalSubServicePrice();

    Long getTotalAccessoryPrice();

    Long getTotalExtraServicePrice();
}
