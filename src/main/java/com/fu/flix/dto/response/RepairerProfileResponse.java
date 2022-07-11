package com.fu.flix.dto.response;

import lombok.Data;

@Data
public class RepairerProfileResponse {
    private String repairerName;
    private Double rating;
    private String experienceDescription;
    private Long experienceYear;
    private String jointAt;
    private Long successfulRepair;
}
