package com.fu.flix.dao.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fu.flix.dao.RedisDAO;
import com.fu.flix.entity.OTPInfo;
import com.fu.flix.dto.request.OTPRequest;
import com.fu.flix.dto.request.RegisterCustomerRequest;
import com.fu.flix.dto.request.RegisterRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class RedisDAOImplTest {

    @Autowired
    RedisDAO redisDAO;

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
        redisDAO.saveOTP(paramOTP);
        OTPInfo resultOTP = redisDAO.findOTP(otpRequest);

        // then
        Assertions.assertEquals(123456, resultOTP.getOtp());
        Assertions.assertEquals("0865390031", resultOTP.getUsername());
    }

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
        otpRequest.setUsername(username);

        // when
        redisDAO.saveOTP(paramOTP);

        Thread.sleep(60000);

        OTPInfo resultOTP = redisDAO.findOTP(otpRequest);

        // then
        Assertions.assertEquals(null, resultOTP);
    }

    @Test
    void test_save_register_account() throws JsonProcessingException {
        // given
        String phone = "0522334455";
        String password = "$2a$10$zbzRvX5G23ZI/qAPkmhQO.sJcVd.YxazNn4HOorXVuyR7V4uf2hvG";
        String fullName = "Ngố Tàu";
        RegisterCustomerRequest request = new RegisterCustomerRequest();
        request.setPassword(password);
        request.setFullName(fullName);
        request.setPhone(phone);

        // when
        redisDAO.saveRegisterAccount(request);
        RegisterRequest result = redisDAO.findRegisterAccount(phone);

        // then
        Assertions.assertEquals(phone, result.getPhone());
        Assertions.assertEquals(password, result.getPassword());
        Assertions.assertEquals(fullName, result.getFullName());
    }

    //    @Test
    void test_register_account_removed_after_60_seconds() throws JsonProcessingException, InterruptedException {
        // given
        String phone = "0522334455";
        String password = "$2a$10$zbzRvX5G23ZI/qAPkmhQO.sJcVd.YxazNn4HOorXVuyR7V4uf2hvG";
        String fullName = "Ngố Tàu";
        RegisterCustomerRequest request = new RegisterCustomerRequest();
        request.setPassword(password);
        request.setFullName(fullName);
        request.setPhone(phone);

        // when
        redisDAO.saveRegisterAccount(request);
        Thread.sleep(60000);
        RegisterRequest result = redisDAO.findRegisterAccount(phone);

        // then
        Assertions.assertEquals(null, result);
    }
}