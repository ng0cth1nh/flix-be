package com.fu.flix.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ea.async.Async;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fu.flix.configuration.AppConf;
import com.fu.flix.dao.*;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.CFForgotPassRequest;
import com.fu.flix.dto.request.CFRegisterRequest;
import com.fu.flix.dto.request.SendForgotPassOTPRequest;
import com.fu.flix.dto.request.SendRegisterOTPRequest;
import com.fu.flix.dto.response.*;
import com.fu.flix.entity.OTPInfo;
import com.fu.flix.entity.User;
import com.fu.flix.service.AccountService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import static com.fu.flix.constant.Constant.*;
import static com.fu.flix.constant.enums.OTPType.FORGOT_PASSWORD;
import static com.fu.flix.constant.enums.OTPType.REGISTER;


@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class AccountServiceImplTest {
    @Autowired
    AccountService underTest;

    @Autowired
    UserDAO userDAO;

    @Autowired
    AppConf appConf;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RedisDAO redisDAO;

    @Autowired
    RepairerDAO repairerDAO;

    @Autowired
    BalanceDAO balanceDAO;

    @Autowired
    UserAddressDAO userAddressDAO;

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

    @Test
    public void test_confirm_otp_request_success() throws IOException {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_PENDING_REPAIRER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        ResponseEntity<CFRegisterResponse> responseEntity = underTest.confirmRegister(request);
        CFRegisterResponse response = responseEntity.getBody();

        // then
        Assertions.assertEquals(CONFIRM_REGISTER_SUCCESS, response.getMessage());
    }

    @Test
    public void test_confirm_otp_request_when_wrong_role_type_and_otp_is_null() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpeg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "customer";
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_ROLE_TYPE, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_when_role_type_is_empty_and_otp_is_null() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.png",
                MediaType.IMAGE_PNG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "";
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_ROLE_TYPE, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_when_role_type_is_null_and_otp_is_null() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.gif",
                MediaType.IMAGE_GIF_VALUE,
                "avatar".getBytes());

        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_ROLE_TYPE, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_when_street_address_is_null() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());

        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String communeId = "00006";
        String roleTpe = "ROLE_CUSTOMER";
        String phone = "0865390031";
        int otp = 123456;

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setPhone(phone);
        request.setRoleType(roleTpe);
        request.setOtp(otp);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtpType(REGISTER);
        otpInfo.setOtp(otp);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_STREET_ADDRESS, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_when_street_address_is_empty() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());

        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String communeId = "00006";
        String roleTpe = "ROLE_CUSTOMER";
        String phone = "0865390031";
        String streetAddress = "";
        int otp = 123456;

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setPhone(phone);
        request.setRoleType(roleTpe);
        request.setOtp(otp);
        request.setStreetAddress(streetAddress);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtpType(REGISTER);
        otpInfo.setOtp(otp);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_STREET_ADDRESS, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_when_commune_is_null() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_PENDING_REPAIRER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(null);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_COMMUNE, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_when_commune_is_empty() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_PENDING_REPAIRER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId("");
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_COMMUNE, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_when_commune_is_abcde() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_PENDING_REPAIRER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId("abcde");
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_COMMUNE, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_when_commune_is_abc05() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_PENDING_REPAIRER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId("abc05");
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_COMMUNE, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_when_commune_is_0() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_PENDING_REPAIRER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId("0");
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_COMMUNE, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_success_with_role_CUSTOMER() throws IOException {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_CUSTOMER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        ResponseEntity<CFRegisterResponse> responseEntity = underTest.confirmRegister(request);
        CFRegisterResponse response = responseEntity.getBody();

        // then
        Assertions.assertEquals(CONFIRM_REGISTER_SUCCESS, response.getMessage());
    }

    @Test
    public void test_confirm_otp_request_when_password_is_123() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_PENDING_REPAIRER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_success_when_password_is_12345abcde() throws IOException {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "12345abcde";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_PENDING_REPAIRER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        ResponseEntity<CFRegisterResponse> responseEntity = underTest.confirmRegister(request);
        CFRegisterResponse response = responseEntity.getBody();

        // then
        Assertions.assertEquals(CONFIRM_REGISTER_SUCCESS, response.getMessage());
    }

    @Test
    public void test_confirm_otp_request_when_password_have_white_space() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123 abc";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_PENDING_REPAIRER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_when_password_length_greater_than_10() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123456abcdefg";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_PENDING_REPAIRER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_fail_when_password_is_abcdefgh() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "abcdefgh";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_PENDING_REPAIRER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_fail_when_password_is_12345678() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "abcdefgh";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_PENDING_REPAIRER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_fail_when_password_have_special_character() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc@";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_PENDING_REPAIRER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_fail_when_password_is_empty() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_PENDING_REPAIRER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_fail_when_password_is_null() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_PENDING_REPAIRER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(null);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_fail_when_full_name_contain_number() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "123 Nhung";
        String password = "123abc";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_CUSTOMER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_FULL_NAME, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_fail_when_full_name_is_empty() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "";
        String password = "123abc";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_CUSTOMER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_FULL_NAME, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_fail_when_full_name_is_null() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String password = "123abc";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_CUSTOMER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(null);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_FULL_NAME, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_fail_when_full_name_contain_number_and_special_character() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nhung @123";
        String password = "123abc";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_CUSTOMER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(INVALID_FULL_NAME, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_success_when_avatar_is_null() throws IOException {
        // given
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_PENDING_REPAIRER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(null);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        ResponseEntity<CFRegisterResponse> responseEntity = underTest.confirmRegister(request);
        CFRegisterResponse response = responseEntity.getBody();

        // then
        Assertions.assertEquals(CONFIRM_REGISTER_SUCCESS, response.getMessage());
    }

    @Test
    public void test_confirm_otp_request_fail_when_avatar_do_not_have_file_extension() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "jpeg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_PENDING_REPAIRER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(FILE_MUST_BE_IMAGE, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_fail_when_avatar_file_extension_is_ptt() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "image.ptt",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_PENDING_REPAIRER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(FILE_MUST_BE_IMAGE, exception.getMessage());
    }

    @Test
    public void test_confirm_otp_request_fail_when_avatar_file_extension_is_txt() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "image.txt",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String roleType = "ROLE_PENDING_REPAIRER";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterRequest request = new CFRegisterRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setRoleType(roleType);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegister(request));

        // then
        Assertions.assertEquals(FILE_MUST_BE_IMAGE, exception.getMessage());
    }

    //    @Test
    public void test_send_forgot_pass_otp_success() throws JsonProcessingException {
        // given
        String phone = "0585943270";
        SendForgotPassOTPRequest request = new SendForgotPassOTPRequest();
        request.setPhone(phone);

        // when
        ResponseEntity<SendForgotPassOTPResponse> responseEntity = underTest.sendForgotPassOTP(request);
        SendForgotPassOTPResponse response = responseEntity.getBody();

        // then
        Assertions.assertEquals(SEND_FORGOT_PASSWORD_OTP_SUCCESS, response.getMessage());
    }

    @Test
    public void test_send_forgot_pass_otp_fail_when_account_not_found() {
        // given
        String phone = "0865390031";
        SendForgotPassOTPRequest request = new SendForgotPassOTPRequest();
        request.setPhone(phone);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.sendForgotPassOTP(request));

        // then
        Assertions.assertEquals(ACCOUNT_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void test_send_forgot_pass_otp_fail_when_phone_is_08653800() {
        // given
        String phone = "08653800";
        SendForgotPassOTPRequest request = new SendForgotPassOTPRequest();
        request.setPhone(phone);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.sendForgotPassOTP(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_send_forgot_pass_otp_fail_when_phone_is_null() {
        // given
        SendForgotPassOTPRequest request = new SendForgotPassOTPRequest();
        request.setPhone(null);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.sendForgotPassOTP(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_send_forgot_pass_otp_fail_when_phone_is_empty() {
        // given
        String phone = "";
        SendForgotPassOTPRequest request = new SendForgotPassOTPRequest();
        request.setPhone(phone);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.sendForgotPassOTP(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_send_forgot_pass_otp_fail_when_phone_is_abc() {
        // given
        String phone = "abc";
        SendForgotPassOTPRequest request = new SendForgotPassOTPRequest();
        request.setPhone(phone);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.sendForgotPassOTP(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_confirm_forgot_password_success() {
        // given
        CFForgotPassRequest request = new CFForgotPassRequest();
        String phone = "0585943270";
        int otp = 123456;
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtpType(FORGOT_PASSWORD);
        otpInfo.setOtp(otp);

        // when
        awaitSaveOTP(otpInfo);
        ResponseEntity<CFForgotPassResponse> responseEntity = underTest.confirmForgotPassword(request);
        CFForgotPassResponse response = responseEntity.getBody();
        String accessToken = response.getAccessToken();

        Algorithm algorithm = Algorithm.HMAC256(this.appConf.getSecretKey().getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(accessToken);

        String username = decodedJWT.getSubject();
        Long id = Long.valueOf(decodedJWT.getId());
        User user = userDAO.findByUsername(username).get();

        // then
        Assertions.assertEquals(id, user.getId());
        Assertions.assertEquals(phone, username);
    }

    @Test
    public void test_confirm_forgot_password_fail_when_phone_is_08653800() {
        // given
        CFForgotPassRequest request = new CFForgotPassRequest();
        String phone = "08653800";
        int otp = 123456;
        request.setOtp(otp);
        request.setPhone(phone);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmForgotPassword(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_confirm_forgot_password_fail_when_phone_is_null() {
        // given
        CFForgotPassRequest request = new CFForgotPassRequest();
        int otp = 123456;
        request.setOtp(otp);
        request.setPhone(null);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmForgotPassword(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_confirm_forgot_password_fail_when_phone_and_otp_not_match() {
        // given
        CFForgotPassRequest request = new CFForgotPassRequest();
        String phone = "0975943816";
        int otp = 123456;
        request.setOtp(otp);
        request.setPhone(phone);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmForgotPassword(request));

        // then
        Assertions.assertEquals(INVALID_OTP, exception.getMessage());
    }

    @Test
    public void test_confirm_forgot_password_fail_when_phone_is_empty() {
        // given
        CFForgotPassRequest request = new CFForgotPassRequest();
        String phone = "";
        int otp = 123456;
        request.setOtp(otp);
        request.setPhone(phone);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmForgotPassword(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_confirm_forgot_password_fail_when_phone_is_abc() {
        // given
        CFForgotPassRequest request = new CFForgotPassRequest();
        String phone = "abc";
        int otp = 123456;
        request.setOtp(otp);
        request.setPhone(phone);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmForgotPassword(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_confirm_forgot_password_fail_when_otp_is_null() {
        // given
        CFForgotPassRequest request = new CFForgotPassRequest();
        String phone = "0585943270";
        request.setOtp(null);
        request.setPhone(phone);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmForgotPassword(request));

        // then
        Assertions.assertEquals(INVALID_OTP, exception.getMessage());
    }

    @Test
    public void test_confirm_forgot_password_fail_when_otp_is_324() {
        // given
        CFForgotPassRequest request = new CFForgotPassRequest();
        String phone = "0585943270";
        int otp = 324;
        request.setOtp(otp);
        request.setPhone(phone);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmForgotPassword(request));

        // then
        Assertions.assertEquals(INVALID_OTP, exception.getMessage());
    }

    @Test
    public void test_confirm_forgot_password_fail_when_otp_is_wrong() {
        // given
        CFForgotPassRequest request = new CFForgotPassRequest();
        String phone = "0585943270";
        int otp = 987654;
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtpType(FORGOT_PASSWORD);
        otpInfo.setOtp(123456);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmForgotPassword(request));

        // then
        Assertions.assertEquals(INVALID_OTP, exception.getMessage());
    }

    private void awaitSaveOTP(OTPInfo otpInfo) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                redisDAO.saveOTP(otpInfo);
            } catch (JsonProcessingException e) {
                throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, SAVE_OTP_FAILED);
            }
        });
        Async.await(future);
    }
}