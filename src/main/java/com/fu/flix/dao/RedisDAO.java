package com.fu.flix.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fu.flix.dto.request.OTPRequest;
import com.fu.flix.entity.OTPInfo;

public interface RedisDAO {
    OTPInfo findOTP(OTPRequest OTPRequest);

    void saveOTP(OTPInfo otpInfo) throws JsonProcessingException;
}
