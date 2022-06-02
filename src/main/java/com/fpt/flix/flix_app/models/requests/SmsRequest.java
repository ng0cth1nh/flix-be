package com.fpt.flix.flix_app.models.requests;

import lombok.Data;

@Data
public class SmsRequest extends DataRequest{
    private String phoneNumber;
}
