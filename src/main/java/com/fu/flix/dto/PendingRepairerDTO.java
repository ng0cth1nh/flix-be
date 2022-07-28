package com.fu.flix.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PendingRepairerDTO {
    private Long id;
    private String repairerName;
    private String repairerPhone;
    private String createdAt;
}
