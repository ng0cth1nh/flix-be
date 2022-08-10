package com.fu.flix.dto;

import java.time.LocalDateTime;

public interface IInvoiceDTO {
    String getCustomerName();

    String getCustomerAvatar();

    String getCustomerPhone();

    String getCustomerAddress();

    String getRepairerName();

    String getRepairerAvatar();

    String getRepairerPhone();

    String getRepairerAddress();

    Long getTotalExtraServicePrice();

    Long getTotalAccessoryPrice();

    Long getTotalSubServicePrice();

    Long getInspectionPrice();

    Long getTotalDiscount();

    LocalDateTime getExpectFixingTime();

    Long getVoucherId();

    String getPaymentMethod();

    String getRequestCode();

    LocalDateTime getCreatedAt();

    Long getActualPrice();

    Long getTotalPrice();

    Long getVatPrice();

    LocalDateTime getApprovedTime();

    String getServiceName();

    String getServiceImage();

    Long getServiceId();

    String getStatus();

    Boolean getIsCustomerCommented();

    Boolean getIsRepairerCommented();
}
