package com.fu.flix.dto;

import java.time.LocalDateTime;

public interface IRequestInfoDTO {
    String getRequestCode();

    Long getCustomerId();

    String getCustomerName();

    String getCustomerPhone();

    Long getRepairerId();

    String getRepairerName();

    String getRepairerPhone();

    String getStatus();

    LocalDateTime getCreatedAt();
}
