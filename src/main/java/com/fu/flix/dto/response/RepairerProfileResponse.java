package com.fu.flix.dto.response;

import lombok.Data;

@Data
public class RepairerProfileResponse {
    private String repairerName;
    private Double rating;
    private String experience;
    private String jointAt;
    private Long successfulRepair;
}
