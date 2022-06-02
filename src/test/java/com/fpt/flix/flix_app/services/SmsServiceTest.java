package com.fpt.flix.flix_app.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.flix.flix_app.models.db.OTPInfo;
import com.fpt.flix.flix_app.models.requests.OTPRequest;
import com.fpt.flix.flix_app.models.requests.SmsRequest;
import com.fpt.flix.flix_app.repositories.RedisRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class SmsServiceTest {

    @Autowired
    SmsService smsService;

    @Autowired
    RedisRepository redisRepository;

    @Test
    void test_send_and_save_OTP() throws JsonProcessingException {
        // given
        String username = "0865390031";
        String phone = "+84865390031";
        SmsRequest request = new SmsRequest();
        request.setUsername(username);
        request.setPhoneNumber(phone);

        // when
        int otp = smsService.sendAndSaveOTP(request);
        OTPRequest otpRequest = new OTPRequest();
        otpRequest.setOtp(otp);
        otpRequest.setUsername(username);
        OTPInfo resultOTP = redisRepository.findOTP(otpRequest);

        // then
        Assertions.assertEquals(otp, resultOTP.getOtp());
        Assertions.assertEquals("0865390031", resultOTP.getUsername());
    }
}