package com.fu.flix.dto;

import java.time.LocalDateTime;

public interface IHistoryRequestForRepairerDTO {
    String getRequestCode();
    String getStatus();
    String getImage();
    String getServiceName();
    String getDescription();
    Double getPrice();
    Double getActualPrice();
    LocalDateTime getCreatedAt();
}
