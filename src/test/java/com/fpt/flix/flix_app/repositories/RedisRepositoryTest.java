package com.fpt.flix.flix_app.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.flix.flix_app.models.db.OTPInfo;
import com.fpt.flix.flix_app.models.requests.OTPRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class RedisRepositoryTest {

    @Autowired
    RedisRepository redisRepository;

    @Test
    void test_save_and_get_OTP_from_redis() throws JsonProcessingException {

        // given
        int otp = 123456;
        String username = "0865390031";

        OTPInfo paramOTP = new OTPInfo();
        paramOTP.setOtp(otp);
        paramOTP.setUsername(username);

        OTPRequest otpRequest = new OTPRequest();
        otpRequest.setOtp(otp);
        otpRequest.setUsername(username);

        // when
        redisRepository.saveOTP(paramOTP);
        OTPInfo resultOTP = redisRepository.findOTP(otpRequest);

        // then
        Assertions.assertEquals(123456, resultOTP.getOtp());
        Assertions.assertEquals("0865390031", resultOTP.getUsername());
    }

    @Test
    void test_OTP_expire_after_60_seconds() throws JsonProcessingException, InterruptedException {

        // given
        int otp = 123456;
        String username = "0865390031";

        OTPInfo paramOTP = new OTPInfo();
        paramOTP.setOtp(otp);
        paramOTP.setUsername(username);

        OTPRequest otpRequest = new OTPRequest();
        otpRequest.setOtp(otp);
        otpRequest.setUsername(username);

        // when
        redisRepository.saveOTP(paramOTP);

        Thread.sleep(60000);

        OTPInfo resultOTP = redisRepository.findOTP(otpRequest);

        // then
        Assertions.assertEquals(null, resultOTP);
    }
}