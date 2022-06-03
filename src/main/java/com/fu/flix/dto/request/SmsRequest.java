package com.fu.flix.dto.request;

import lombok.Data;

@Data
public class SmsRequest extends DataRequest{
    private String phoneNumber;
}
