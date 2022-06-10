package com.fu.flix.dto.response;

import lombok.Data;

@Data
public class RequestingRepairResponse {
    private String requestCode;
    private String status;
    private String message;
}
