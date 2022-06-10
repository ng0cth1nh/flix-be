package com.fu.flix.dao.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fu.flix.dao.RedisDAO;
import com.fu.flix.entity.OTPInfo;
import com.fu.flix.dto.request.OTPRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RunWith(SpringRunner.class)
@SpringBootTest
class RedisDAOImplTest {

    @Autowired
    RedisDAO redisDAO;

    //    @Test
    void test_OTP_expire_after_60_seconds() throws JsonProcessingException, InterruptedException {

        // given
        int otp = 123456;
        String username = "0865390031";

        OTPInfo paramOTP = new OTPInfo();
        paramOTP.setOtp(otp);
        paramOTP.setUsername(username);

        OTPRequest otpRequest = new OTPRequest();
        otpRequest.setOtp(otp);
        otpRequest.setPhone(username);

        // when
        redisDAO.saveOTP(paramOTP);

        Thread.sleep(60000);

        OTPInfo resultOTP = redisDAO.findOTP(otpRequest);

        // then
        Assertions.assertEquals(null, resultOTP);
    }

}