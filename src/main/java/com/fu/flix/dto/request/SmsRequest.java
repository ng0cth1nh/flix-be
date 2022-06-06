package com.fu.flix.dto.request;

import lombok.Data;

@Data
public class SmsRequest {
    private String phoneNumber;
    private String username;
}
