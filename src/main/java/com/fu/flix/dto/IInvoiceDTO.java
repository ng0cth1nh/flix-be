package com.fu.flix.dto;

import java.time.LocalDateTime;

public interface IInvoiceDTO {
    String getCustomerName();

    String getCustomerAvatar();

    String getCustomerPhone();

    Long getCustomerAddressId();

    String getRepairerName();

    String getRepairerAvatar();

    String getRepairerPhone();

    Long getRepairerAddressId();

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
}
