package com.fu.flix.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fu.flix.dto.request.SmsRequest;

public interface SmsService {
    int sendAndSaveOTP(SmsRequest request) throws JsonProcessingException;
}
