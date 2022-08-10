package com.fu.flix.dto;

import java.time.LocalDateTime;

public interface IDetailFixingRequestForCustomerDTO {
    String getStatus();

    String getServiceImage();

    Long getServiceId();

    String getServiceName();

    String getCustomerAddress();

    String getCustomerPhone();

    String getCustomerName();

    LocalDateTime getExpectFixingDay();

    String getRequestDescription();

    Long getVoucherId();

    String getPaymentMethod();

    LocalDateTime getCreatedAt();

    Long getTotalPrice();

    Long getActualPrice();

    Long getVatPrice();

    String getRequestCode();

    String getRepairerAddress();

    String getRepairerPhone();

    String getRepairerName();

    Long getRepairerId();

    String getRepairerAvatar();

    Long getInspectionPrice();

    Long getTotalDiscount();
    LocalDateTime getApprovedTime();
}
