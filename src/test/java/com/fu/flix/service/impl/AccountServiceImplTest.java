package com.fu.flix.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fu.flix.configuration.AppConf;
import com.fu.flix.dao.UserDAO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.SendRegisterOTPRequest;
import com.fu.flix.dto.response.SendRegisterOTPResponse;
import com.fu.flix.dto.response.TokenResponse;
import com.fu.flix.service.AccountService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Date;

import static com.fu.flix.constant.Constant.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;


@RunWith(SpringRunner.class)
@SpringBootTest
@TestInstance(PER_CLASS)
class AccountServiceImplTest {
    @Autowired
    AccountService underTest;

    @Autowired
    UserDAO userDAO;

    @Autowired
    AppConf appConf;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void test_refresh_token_valid() throws IOException {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpServletRequest request = new MockHttpServletRequest();

        Long userId = 36L;
        String username = "0865390037";

        Algorithm algorithm = Algorithm.HMAC256(this.appConf.getSecretKey().getBytes());
        String refreshToken = JWT.create()
                .withJWTId(String.valueOf(userId))
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + this.appConf.getLifeTimeRefreshToken()))
                .sign(algorithm);

        request.addHeader("Authorization", "Bearer " + refreshToken);

        // when
        underTest.refreshToken(request, response);
        String contentAsString = response.getContentAsString();
        TokenResponse tokenResponse = objectMapper.readValue(contentAsString, TokenResponse.class);
        String accessToken = tokenResponse.getAccessToken();

        // then
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(accessToken);

        Assertions.assertEquals(username, decodedJWT.getSubject());
    }

    @Test
    public void test_refresh_token_empty() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader("Authorization", "");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.refreshToken(request, response));

        // then
        Assertions.assertEquals(REFRESH_TOKEN_MISSING, exception.getMessage());
    }

    @Test
    public void test_refresh_token_wrong() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpServletRequest request = new MockHttpServletRequest();

        String refreshToken = "something";
        request.addHeader("Authorization", "Bearer " + refreshToken);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.refreshToken(request, response));

        // then
        Assertions.assertEquals(INVALID_REFRESH_TOKEN, exception.getMessage());
    }

    @Test
    public void test_refresh_token_null() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpServletRequest request = new MockHttpServletRequest();

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.refreshToken(request, response));

        // then
        Assertions.assertEquals(REFRESH_TOKEN_MISSING, exception.getMessage());
    }

//    @Test
    public void test_send_register_otp_with_phone_valid() throws JsonProcessingException {
        // given
        SendRegisterOTPRequest request = new SendRegisterOTPRequest();
        request.setPhone("0865390031");

        // when
        ResponseEntity<SendRegisterOTPResponse> responseEntity = underTest.sendRegisterOTP(request);
        SendRegisterOTPResponse response = responseEntity.getBody();

        // then
        Assertions.assertEquals(NEW_ACCOUNT_VALID, response.getMessage());
    }

    @Test
    public void test_send_register_otp_with_phone_length_too_short() {
        // given
        SendRegisterOTPRequest request = new SendRegisterOTPRequest();
        String phone = "086538000";
        request.setPhone(phone);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.sendRegisterOTP(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_send_register_otp_with_phone_length_too_long() {
        // given
        SendRegisterOTPRequest request = new SendRegisterOTPRequest();
        request.setPhone("08653900311");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.sendRegisterOTP(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_send_register_otp_with_phone_length_is_null() {
        // given
        SendRegisterOTPRequest request = new SendRegisterOTPRequest();
        request.setPhone(null);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.sendRegisterOTP(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_send_register_otp_with_phone_length_is_empty() {
        // given
        SendRegisterOTPRequest request = new SendRegisterOTPRequest();
        request.setPhone(null);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.sendRegisterOTP(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_send_register_otp_with_phone_length_is_have_alphabet() {
        // given
        SendRegisterOTPRequest request = new SendRegisterOTPRequest();
        request.setPhone("abc");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.sendRegisterOTP(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_send_register_otp_when_account_existed() {
        // given
        SendRegisterOTPRequest request = new SendRegisterOTPRequest();
        String phone = "0962706248";

        request.setPhone(phone);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.sendRegisterOTP(request));

        // then
        Assertions.assertEquals(ACCOUNT_EXISTED, exception.getMessage());
    }
}