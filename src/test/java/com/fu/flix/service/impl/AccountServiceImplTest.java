package com.fu.flix.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ea.async.Async;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.enums.ApplicationType;
import com.fu.flix.constant.enums.RoleType;
import com.fu.flix.dao.*;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.*;
import com.fu.flix.dto.response.*;
import com.fu.flix.dto.security.UserPrincipal;
import com.fu.flix.entity.OTPInfo;
import com.fu.flix.entity.User;
import com.fu.flix.service.AccountService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
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
                .withClaim(USER_ID, userId)
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

    @Test
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
    public void test_confirm_register_customer_success() throws IOException {
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
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        ResponseEntity<CFRegisterCustomerResponse> responseEntity = underTest.confirmRegisterCustomer(request);
        CFRegisterCustomerResponse response = responseEntity.getBody();

        // then
        Assertions.assertEquals(CONFIRM_REGISTER_SUCCESS, response.getMessage());
    }

    @Test
    public void test_confirm_register_customer_when_street_address_is_null() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());

        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String communeId = "00006";
        String phone = "0865390031";
        int otp = 123456;

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setPhone(phone);
        request.setOtp(otp);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtpType(REGISTER);
        otpInfo.setOtp(otp);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(INVALID_STREET_ADDRESS, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_when_street_address_is_empty() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());

        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String communeId = "00006";
        String phone = "0865390031";
        String streetAddress = "";
        int otp = 123456;

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setPhone(phone);
        request.setOtp(otp);
        request.setStreetAddress(streetAddress);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtpType(REGISTER);
        otpInfo.setOtp(otp);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(INVALID_STREET_ADDRESS, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_when_commune_is_null() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String streetAddress = "Đường 30m Hòa Lạc";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(null);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(INVALID_COMMUNE, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_when_commune_is_empty() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String streetAddress = "Đường 30m Hòa Lạc";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId("");
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(INVALID_COMMUNE, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_when_commune_is_abcde() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String streetAddress = "Đường 30m Hòa Lạc";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId("abcde");
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(INVALID_COMMUNE, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_when_commune_is_abc05() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String streetAddress = "Đường 30m Hòa Lạc";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId("abc05");
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(INVALID_COMMUNE, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_when_commune_is_0() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String streetAddress = "Đường 30m Hòa Lạc";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId("0");
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(INVALID_COMMUNE, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_success_with_role_CUSTOMER() throws IOException {
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
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        ResponseEntity<CFRegisterCustomerResponse> responseEntity = underTest.confirmRegisterCustomer(request);
        CFRegisterCustomerResponse response = responseEntity.getBody();

        // then
        Assertions.assertEquals(CONFIRM_REGISTER_SUCCESS, response.getMessage());
    }

    @Test
    public void test_confirm_register_customer_when_password_is_123() {
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
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_success_when_password_is_12345abcde() throws IOException {
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
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        ResponseEntity<CFRegisterCustomerResponse> responseEntity = underTest.confirmRegisterCustomer(request);
        CFRegisterCustomerResponse response = responseEntity.getBody();

        // then
        Assertions.assertEquals(CONFIRM_REGISTER_SUCCESS, response.getMessage());
    }

    @Test
    public void test_confirm_register_customer_when_password_have_white_space() {
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
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_when_password_length_greater_than_10() {
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
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_fail_when_password_is_abcdefgh() {
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
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_fail_when_password_is_12345678() {
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
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_fail_when_password_have_special_character() {
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
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_fail_when_password_is_empty() {
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
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_fail_when_password_is_null() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(null);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_fail_when_full_name_contain_number() {
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
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(INVALID_FULL_NAME, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_fail_when_full_name_is_empty() {
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
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(INVALID_FULL_NAME, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_fail_when_full_name_is_null() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String password = "123abc";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(null);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(INVALID_FULL_NAME, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_fail_when_full_name_contain_number_and_special_character() {
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
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(INVALID_FULL_NAME, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_success_when_avatar_is_null() throws IOException {
        // given
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(null);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        ResponseEntity<CFRegisterCustomerResponse> responseEntity = underTest.confirmRegisterCustomer(request);
        CFRegisterCustomerResponse response = responseEntity.getBody();

        // then
        Assertions.assertEquals(CONFIRM_REGISTER_SUCCESS, response.getMessage());
    }

    @Test
    public void test_confirm_register_customer_fail_when_avatar_do_not_have_file_extension() {
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
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(FILE_MUST_BE_IMAGE, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_fail_when_avatar_file_extension_is_ptt() {
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
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(FILE_MUST_BE_IMAGE, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_fail_when_avatar_file_extension_is_txt() {
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
        int otp = 123456;
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(FILE_MUST_BE_IMAGE, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_fail_when_invalid_phone() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        int otp = 123456;
        String phone = "08653900";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_fail_when_account_existed() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        int otp = 123456;
        String phone = "0865390037";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(otp);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(ACCOUNT_EXISTED, exception.getMessage());
    }

    @Test
    public void test_confirm_register_customer_fail_when_otp_wrong() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        String fullName = "Nguyễn Thị Hồng Nhung";
        String password = "123abc";
        String communeId = "00006";
        String streetAddress = "Đường 30m Hòa Lạc";
        String phone = "0865390031";

        CFRegisterCustomerRequest request = new CFRegisterCustomerRequest();
        request.setAvatar(avatar);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(123456);
        request.setPhone(phone);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(phone);
        otpInfo.setOtp(123455);
        otpInfo.setOtpType(REGISTER);

        // when
        awaitSaveOTP(otpInfo);
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterCustomer(request));

        // then
        Assertions.assertEquals(INVALID_OTP, exception.getMessage());
    }

    @Test
    public void test_send_forgot_pass_otp_success() throws JsonProcessingException {
        // given
        String phone = "0585943270";
        SendForgotPassOTPRequest request = new SendForgotPassOTPRequest();
        request.setPhone(phone);
        request.setRoleType(ApplicationType.CUSTOMER.name());

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
    public void test_send_forgot_pass_otp_fail_when_wrong_application_type() {
        // given
        String phone = "0865390037";
        SendForgotPassOTPRequest request = new SendForgotPassOTPRequest();
        request.setPhone(phone);
        request.setRoleType("ADMIN");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.sendForgotPassOTP(request));

        // then
        Assertions.assertEquals(ACCOUNT_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void test_confirm_forgot_password_success() {
        // given
        CFForgotPassRequest request = new CFForgotPassRequest();
        String phone = "0865390037";
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
        Long id = decodedJWT.getClaim(USER_ID).asLong();
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

    @Test
    public void test_reset_password_success() {
        // given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("123abc");

        // when
        setCustomerContext(36L, "0865390037");
        ResetPasswordResponse response = underTest.resetPassword(request).getBody();

        // then
        Assertions.assertEquals(RESET_PASSWORD_SUCCESS, response.getMessage());
    }

    @Test
    public void test_reset_password_fail_when_password_is_123() {
        // given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("123");

        // when
        setCustomerContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.resetPassword(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_reset_password_fail_when_password_contain_white_space() {
        // given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("123 abc");

        // when
        setCustomerContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.resetPassword(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_reset_password_fail_when_password_is_too_long() {
        // given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("123456abcdefg");

        // when
        setCustomerContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.resetPassword(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_reset_password_fail_when_password_is_abcdefgh() {
        // given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("abcdefgh");

        // when
        setCustomerContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.resetPassword(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_reset_password_fail_when_password_is_1234567() {
        // given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("1234567");

        // when
        setCustomerContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.resetPassword(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_reset_password_fail_when_password_is_contain_special_character() {
        // given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("123abc@");

        // when
        setCustomerContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.resetPassword(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_reset_password_fail_when_password_is_empty() {
        // given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("@");

        // when
        setCustomerContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.resetPassword(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_reset_password_fail_when_password_is_null() {
        // given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword(null);

        // when
        setCustomerContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.resetPassword(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_confirm_register_repairer_success() throws IOException {
        // given
        CFRegisterRepairerRequest request = getCfRegisterRepairerRequestValidated();

        List<Long> serviceIds = new ArrayList<>();
        serviceIds.add(1L);

        request.setRegisterServices(serviceIds);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtp(request.getOtp());
        otpInfo.setOtpType(REGISTER);

        awaitSaveOTP(otpInfo);

        // when
        CFRegisterRepairerResponse response = underTest.confirmRegisterRepairer(request).getBody();

        // then
        Assertions.assertEquals(CONFIRM_REGISTER_SUCCESS, response.getMessage());
    }

    @Test
    public void test_confirm_register_repairer_fail_when_phone_is_invalid() {
        // given
        CFRegisterRepairerRequest request = getCfRegisterRepairerRequestValidated();
        request.setPhone("0865");

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtp(request.getOtp());
        otpInfo.setOtpType(REGISTER);

        awaitSaveOTP(otpInfo);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterRepairer(request));

        // then
        Assertions.assertEquals(INVALID_PHONE_NUMBER, exception.getMessage());
    }

    @Test
    public void test_confirm_register_repairer_fail_when_account_existed() {
        // given
        CFRegisterRepairerRequest request = getCfRegisterRepairerRequestValidated();
        request.setPhone("0865390037");

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtp(request.getOtp());
        otpInfo.setOtpType(REGISTER);

        awaitSaveOTP(otpInfo);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterRepairer(request));

        // then
        Assertions.assertEquals(ACCOUNT_EXISTED, exception.getMessage());
    }

    @Test
    public void test_confirm_register_repairer_fail_when_password_invalid() {
        // given
        CFRegisterRepairerRequest request = getCfRegisterRepairerRequestValidated();
        request.setPassword("123");

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtp(request.getOtp());
        otpInfo.setOtpType(REGISTER);

        awaitSaveOTP(otpInfo);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterRepairer(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_confirm_register_repairer_fail_when_commune_invalid() {
        // given
        CFRegisterRepairerRequest request = getCfRegisterRepairerRequestValidated();
        request.setCommuneId("01");

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtp(request.getOtp());
        otpInfo.setOtpType(REGISTER);

        awaitSaveOTP(otpInfo);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterRepairer(request));

        // then
        Assertions.assertEquals(INVALID_COMMUNE, exception.getMessage());
    }

    @Test
    public void test_confirm_register_repairer_fail_when_full_name_is_null() {
        // given
        CFRegisterRepairerRequest request = getCfRegisterRepairerRequestValidated();
        request.setFullName(null);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtp(request.getOtp());
        otpInfo.setOtpType(REGISTER);

        awaitSaveOTP(otpInfo);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterRepairer(request));

        // then
        Assertions.assertEquals(INVALID_FULL_NAME, exception.getMessage());
    }

    @Test
    public void test_confirm_register_repairer_fail_when_street_address_is_null() {
        // given
        CFRegisterRepairerRequest request = getCfRegisterRepairerRequestValidated();
        request.setStreetAddress(null);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtp(request.getOtp());
        otpInfo.setOtpType(REGISTER);

        awaitSaveOTP(otpInfo);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterRepairer(request));

        // then
        Assertions.assertEquals(INVALID_STREET_ADDRESS, exception.getMessage());
    }

    @Test
    public void test_confirm_register_repairer_fail_when_experience_description_is_null() {
        // given
        CFRegisterRepairerRequest request = getCfRegisterRepairerRequestValidated();
        request.setExperienceDescription(null);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtp(request.getOtp());
        otpInfo.setOtpType(REGISTER);

        awaitSaveOTP(otpInfo);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterRepairer(request));

        // then
        Assertions.assertEquals(INVALID_EXPERIENCE_DESCRIPTION, exception.getMessage());
    }

    @Test
    public void test_confirm_register_repairer_fail_when_certificates_invalid() {
        // given
        MockMultipartFile certi1 = new MockMultipartFile(
                "certi1",
                "certi1.txt",
                MediaType.TEXT_XML_VALUE,
                "certi1".getBytes());
        List<MultipartFile> certificates = new ArrayList<>();
        certificates.add(certi1);

        CFRegisterRepairerRequest request = getCfRegisterRepairerRequestValidated();
        request.setCertificates(certificates);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtp(request.getOtp());
        otpInfo.setOtpType(REGISTER);

        awaitSaveOTP(otpInfo);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterRepairer(request));

        // then
        Assertions.assertEquals(CERTIFICATE_FILE_MUST_BE_IMAGE_OR_PDF, exception.getMessage());
    }

    @Test
    public void test_confirm_register_repairer_success_when_certificates_is_null() throws IOException {
        // given
        CFRegisterRepairerRequest request = getCfRegisterRepairerRequestValidated();
        request.setCertificates(null);

        List<Long> serviceIds = new ArrayList<>();
        serviceIds.add(1L);
        request.setRegisterServices(serviceIds);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtp(request.getOtp());
        otpInfo.setOtpType(REGISTER);

        awaitSaveOTP(otpInfo);

        // when
        CFRegisterRepairerResponse response = underTest.confirmRegisterRepairer(request).getBody();

        // then
        Assertions.assertEquals(CONFIRM_REGISTER_SUCCESS, response.getMessage());
    }

    @Test
    public void test_confirm_register_repairer_fail_when_experience_years_is_null() {
        // given
        CFRegisterRepairerRequest request = getCfRegisterRepairerRequestValidated();
        request.setExperienceYear(null);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtp(request.getOtp());
        otpInfo.setOtpType(REGISTER);

        awaitSaveOTP(otpInfo);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterRepairer(request));

        // then
        Assertions.assertEquals(INVALID_EXPERIENCE_YEARS, exception.getMessage());
    }

    @Test
    public void test_confirm_register_repairer_fail_when_front_image_is_null() {
        // given
        CFRegisterRepairerRequest request = getCfRegisterRepairerRequestValidated();
        request.setFrontImage(null);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtp(request.getOtp());
        otpInfo.setOtpType(REGISTER);

        awaitSaveOTP(otpInfo);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterRepairer(request));

        // then
        Assertions.assertEquals(FRONT_IMAGE_MUST_BE_IMAGE, exception.getMessage());
    }

    @Test
    public void test_confirm_register_repairer_fail_when_back_image_is_null() {
        // given
        CFRegisterRepairerRequest request = getCfRegisterRepairerRequestValidated();
        request.setBackSideImage(null);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtp(request.getOtp());
        otpInfo.setOtpType(REGISTER);

        awaitSaveOTP(otpInfo);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterRepairer(request));

        // then
        Assertions.assertEquals(BACK_SIDE_IMAGE_MUST_BE_IMAGE, exception.getMessage());
    }

    @Test
    public void test_confirm_register_repairer_fail_when_identity_number_invalid() {
        // given
        CFRegisterRepairerRequest request = getCfRegisterRepairerRequestValidated();
        request.setIdentityCardNumber("123");

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtp(request.getOtp());
        otpInfo.setOtpType(REGISTER);

        awaitSaveOTP(otpInfo);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterRepairer(request));

        // then
        Assertions.assertEquals(INVALID_IDENTITY_CARD_NUMBER, exception.getMessage());
    }

    @Test
    public void test_confirm_register_repairer_fail_when_identity_type_invalid() {
        // given
        CFRegisterRepairerRequest request = getCfRegisterRepairerRequestValidated();
        request.setIdentityCardType("CC");

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtp(request.getOtp());
        otpInfo.setOtpType(REGISTER);

        awaitSaveOTP(otpInfo);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterRepairer(request));

        // then
        Assertions.assertEquals(IDENTITY_CARD_TYPE_MUST_BE_CCCD_OR_CMND, exception.getMessage());
    }

    @Test
    public void test_confirm_register_repairer_fail_when_gender_is_null() {
        // given
        CFRegisterRepairerRequest request = getCfRegisterRepairerRequestValidated();
        request.setGender(null);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtp(request.getOtp());
        otpInfo.setOtpType(REGISTER);

        awaitSaveOTP(otpInfo);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterRepairer(request));

        // then
        Assertions.assertEquals(GENDER_IS_REQUIRED, exception.getMessage());
    }

    @Test
    public void test_confirm_register_repairer_fail_when_dob_is_null() {
        // given
        CFRegisterRepairerRequest request = getCfRegisterRepairerRequestValidated();
        request.setDateOfBirth(null);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtp(request.getOtp());
        otpInfo.setOtpType(REGISTER);

        awaitSaveOTP(otpInfo);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterRepairer(request));

        // then
        Assertions.assertEquals(INVALID_DATE_OF_BIRTH, exception.getMessage());
    }

    @Test
    public void test_confirm_register_repairer_fail_when_identity_number_existed() {
        // given
        CFRegisterRepairerRequest request = getCfRegisterRepairerRequestValidated();
        request.setIdentityCardNumber("0343432444");

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtp(request.getOtp());
        otpInfo.setOtpType(REGISTER);

        awaitSaveOTP(otpInfo);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterRepairer(request));

        // then
        Assertions.assertEquals(IDENTITY_CARD_NUMBER_EXISTED, exception.getMessage());
    }

    @Test
    public void test_confirm_register_repairer_fail_when_invalid_otp() {
        // given
        CFRegisterRepairerRequest request = getCfRegisterRepairerRequestValidated();
        request.setOtp(null);

        List<Long> serviceIds = new ArrayList<>();
        serviceIds.add(1L);
        request.setRegisterServices(serviceIds);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtp(123456);
        otpInfo.setOtpType(REGISTER);

        awaitSaveOTP(otpInfo);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterRepairer(request));

        // then
        Assertions.assertEquals(INVALID_OTP, exception.getMessage());
    }

    @Test
    public void test_confirm_register_repairer_fail_when_commune_id_is_null() {
        // given
        CFRegisterRepairerRequest request = getCfRegisterRepairerRequestValidated();
        request.setCommuneId(null);

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtp(request.getOtp());
        otpInfo.setOtpType(REGISTER);

        awaitSaveOTP(otpInfo);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterRepairer(request));

        // then
        Assertions.assertEquals(INVALID_COMMUNE, exception.getMessage());
    }

    @Test
    public void test_confirm_register_repairer_fail_when_register_services_is_empty() {
        // given
        CFRegisterRepairerRequest request = getCfRegisterRepairerRequestValidated();
        request.setCommuneId("00001");
        request.setRegisterServices(new ArrayList<>());

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtp(request.getOtp());
        otpInfo.setOtpType(REGISTER);

        awaitSaveOTP(otpInfo);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.confirmRegisterRepairer(request));

        // then
        Assertions.assertEquals(INVALID_REGISTER_SERVICE_IDS, exception.getMessage());
    }

    private CFRegisterRepairerRequest getCfRegisterRepairerRequestValidated() {
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());

        String password = "123abc";

        String identityCardNumber = "0756283956";

        String identityCardType = "CCCD";

        MockMultipartFile frontImage = new MockMultipartFile(
                "front_image",
                "front.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "front".getBytes());

        MockMultipartFile backSideImage = new MockMultipartFile(
                "back_image",
                "back.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "back".getBytes());

        Integer experienceYear = 3;

        String experienceDescription = "pro vip";

        MockMultipartFile certi1 = new MockMultipartFile(
                "certi1",
                "certi1.jpg",
                MediaType.APPLICATION_PDF_VALUE,
                "certi1".getBytes());
        List<MultipartFile> certificates = new ArrayList<>();
        certificates.add(certi1);

        Boolean gender = true;

        String dateOfBirth = "08-03-2000";

        String fullName = "Nguyễn Thị Hồng Nhung";

        String communeId = "00006";

        String streetAddress = "Đường 30m Hòa Lạc";

        int otp = 123456;

        String phone = "0865390031";

        CFRegisterRepairerRequest request = new CFRegisterRepairerRequest();
        request.setAvatar(avatar);
        request.setPassword(password);
        request.setIdentityCardNumber(identityCardNumber);
        request.setIdentityCardType(identityCardType);
        request.setFrontImage(frontImage);
        request.setBackSideImage(backSideImage);
        request.setExperienceYear(experienceYear);
        request.setExperienceDescription(experienceDescription);
        request.setCertificates(certificates);
        request.setGender(gender);
        request.setDateOfBirth(dateOfBirth);
        request.setFullName(fullName);
        request.setCommuneId(communeId);
        request.setStreetAddress(streetAddress);
        request.setOtp(otp);
        request.setPhone(phone);

        return request;
    }

    @Test
    void test_login_success_for_repairer() {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("0865390051");
        request.setPassword("123abc");
        request.setRoleType("REPAIRER");

        // when
        LoginResponse response = underTest.login(request).getBody();

        // then
        Assertions.assertNotNull(response);
    }

    @Test
    void test_login_success_for_admin_manager() {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("0865390062");
        request.setPassword("123abc");
        request.setRoleType("ADMIN");

        // when
        LoginResponse response = underTest.login(request).getBody();

        // then
        Assertions.assertNotNull(response);
    }

    @Test
    void test_login_success_for_admin_customer() {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("0865390037");
        request.setPassword("123abc");
        request.setRoleType("CUSTOMER");

        // when
        LoginResponse response = underTest.login(request).getBody();

        // then
        Assertions.assertNotNull(response);
    }

    @Test
    void test_login_success_for_pending_repairer() {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("0865390068");
        request.setPassword("123abc");
        request.setRoleType("REPAIRER");

        // when
        LoginResponse response = underTest.login(request).getBody();

        // then
        Assertions.assertNotNull(response);
    }

    @Test
    void test_login_fail_when_invalid_type() {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("0865390051");
        request.setPassword("123abc");
        request.setRoleType("abc");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.login(request));

        // then
        Assertions.assertEquals(INVALID_TYPE, exception.getMessage());
    }

    @Test
    void test_login_fail_when_login_type_not_match() {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("0865390051");
        request.setPassword("123abc");
        request.setRoleType("ADMIN");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.login(request));

        // then
        Assertions.assertEquals(LOGIN_FAILED, exception.getMessage());
    }

    @Test
    void test_login_fail_when_invalid_phone() {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("123");
        request.setPassword("123abc");
        request.setRoleType("REPAIRER");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.login(request));

        // then
        Assertions.assertEquals(LOGIN_FAILED, exception.getMessage());
    }

    @Test
    void test_login_fail_when_user_not_found() {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("0865111111");
        request.setPassword("123abc");
        request.setRoleType("REPAIRER");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.login(request));

        // then
        Assertions.assertEquals(LOGIN_FAILED, exception.getMessage());
    }

    @Test
    void test_login_fail_when_password_not_match() {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("0865390051");
        request.setPassword("abc123");
        request.setRoleType("REPAIRER");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.login(request));

        // then
        Assertions.assertEquals(LOGIN_FAILED, exception.getMessage());
    }

    @Test
    void test_login_fail_when_user_is_inactive() {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("0865390041");
        request.setPassword("123abc");
        request.setRoleType("CUSTOMER");

        userDAO.findByUsername("0865390041").get().setIsActive(false);

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.login(request));

        // then
        Assertions.assertEquals(USER_IS_INACTIVE, exception.getMessage());
    }

    @Test
    void test_isInvalidRegisterServices_when_list_id_is_empty() {
        // given
        List<Long> registerServices = new ArrayList<>();

        // when
        boolean check = underTest.isInvalidRegisterServices(registerServices);

        // then
        Assertions.assertTrue(check);
    }

    void setCustomerContext(Long id, String phone) {
        List<String> roles = new ArrayList<>();
        roles.add(RoleType.ROLE_CUSTOMER.name());
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(new UserPrincipal(id, phone, roles), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}