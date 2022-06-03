package com.fpt.flix.flix_app.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.flix.flix_app.models.db.OTPInfo;
import com.fpt.flix.flix_app.models.requests.OTPRequest;
import com.fpt.flix.flix_app.models.requests.RegisterCustomerRequest;
import com.fpt.flix.flix_app.models.requests.RegisterRequest;
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

    @Test
    void test_save_register_account() throws JsonProcessingException {
        // given
        String phone = "0522334455";
        String password = "$2a$10$zbzRvX5G23ZI/qAPkmhQO.sJcVd.YxazNn4HOorXVuyR7V4uf2hvG";
        String firstName = "Ngố";
        String lastName = "Tàu";
        RegisterCustomerRequest request = new RegisterCustomerRequest();
        request.setPassword(password);
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setPhone(phone);

        // when
        redisRepository.saveRegisterAccount(request);
        RegisterRequest result = redisRepository.findRegisterAccount(phone);

        // then
        Assertions.assertEquals(phone, result.getPhone());
        Assertions.assertEquals(password, result.getPassword());
        Assertions.assertEquals(firstName, result.getFirstName());
        Assertions.assertEquals(lastName, result.getLastName());
    }

    @Test
    void test_register_account_removed_after_60_seconds() throws JsonProcessingException, InterruptedException {
        // given
        String phone = "0522334455";
        String password = "$2a$10$zbzRvX5G23ZI/qAPkmhQO.sJcVd.YxazNn4HOorXVuyR7V4uf2hvG";
        String firstName = "Ngố";
        String lastName = "Tàu";
        RegisterCustomerRequest request = new RegisterCustomerRequest();
        request.setPassword(password);
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setPhone(phone);

        // when
        redisRepository.saveRegisterAccount(request);
        Thread.sleep(60000);
        RegisterRequest result = redisRepository.findRegisterAccount(phone);

        // then
        Assertions.assertEquals(null, result);
    }
}