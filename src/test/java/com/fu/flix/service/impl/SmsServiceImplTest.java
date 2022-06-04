package com.fu.flix.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fu.flix.entity.OTPInfo;
import com.fu.flix.dto.request.OTPRequest;
import com.fu.flix.dto.request.SmsRequest;
import com.fu.flix.dao.impl.RedisDAOImpl;
import com.fu.flix.service.impl.SmsServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class SmsServiceImplTest {

    @Autowired
    SmsServiceImpl smsServiceImpl;

    @Autowired
    RedisDAOImpl redisDAOImpl;

//    @Test
    void test_send_and_save_OTP() throws JsonProcessingException {
        // given
        String username = "0865390031";
        String phone = "+84865390031";
        SmsRequest request = new SmsRequest();
        request.setUsername(username);
        request.setPhoneNumber(phone);

        // when
        int otp = smsServiceImpl.sendAndSaveOTP(request);
        OTPRequest otpRequest = new OTPRequest();
        otpRequest.setOtp(otp);
        otpRequest.setUsername(username);
        OTPInfo resultOTP = redisDAOImpl.findOTP(otpRequest);

        // then
        Assertions.assertEquals(otp, resultOTP.getOtp());
        Assertions.assertEquals("0865390031", resultOTP.getUsername());
    }
}