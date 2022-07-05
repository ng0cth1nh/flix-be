package com.fu.flix.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.enums.OTPType;
import com.fu.flix.constant.enums.RoleType;
import com.fu.flix.constant.enums.TokenType;
import com.fu.flix.dao.*;
import com.fu.flix.dto.PhoneDTO;
import com.fu.flix.dto.security.UserSecurity;
import com.fu.flix.entity.*;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.response.*;
import com.fu.flix.service.*;
import com.fu.flix.util.InputValidation;
import com.fu.flix.util.PhoneFormatter;
import com.fu.flix.dto.request.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.fu.flix.constant.Constant.*;
import static com.fu.flix.constant.enums.RoleType.ROLE_CUSTOMER;
import static com.fu.flix.constant.enums.RoleType.ROLE_PENDING_REPAIRER;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@Transactional
@Slf4j
public class AccountServiceImpl implements UserDetailsService, AccountService {
    private final UserDAO userDAO;
    private final RoleDAO roleDAO;
    private final AppConf appConf;
    private final RedisDAO redisDAO;
    private final SmsService smsService;
    private final CommuneDAO communeDAO;
    private final DistrictDAO districtDAO;
    private final CityDAO cityDAO;
    private final UserAddressDAO userAddressDAO;
    private final PasswordEncoder passwordEncoder;
    private final CloudStorageService cloudStorageService;
    private final UserService userService;
    private final RepairerDAO repairerDAO;
    private final BalanceDAO balanceDAO;
    private final ValidatorService validatorService;


    public AccountServiceImpl(UserDAO userDAO,
                              RoleDAO roleDAO,
                              AppConf appConf,
                              RedisDAO redisDAO,
                              SmsService smsService,
                              CommuneDAO communeDAO,
                              DistrictDAO districtDAO,
                              CityDAO cityDAO,
                              UserAddressDAO userAddressDAO,
                              PasswordEncoder passwordEncoder,
                              CloudStorageService cloudStorageService,
                              UserService userService,
                              RepairerDAO repairerDAO,
                              BalanceDAO balanceDAO,
                              ValidatorService validatorService) {
        this.userDAO = userDAO;
        this.roleDAO = roleDAO;
        this.appConf = appConf;
        this.redisDAO = redisDAO;
        this.smsService = smsService;
        this.communeDAO = communeDAO;
        this.districtDAO = districtDAO;
        this.cityDAO = cityDAO;
        this.userAddressDAO = userAddressDAO;
        this.passwordEncoder = passwordEncoder;
        this.cloudStorageService = cloudStorageService;
        this.userService = userService;
        this.repairerDAO = repairerDAO;
        this.balanceDAO = balanceDAO;
        this.validatorService = validatorService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userDAO.findByUsername(username);
        if (optionalUser.isEmpty()) {
            log.error("User {} not found", username);
            throw new GeneralException(HttpStatus.FORBIDDEN, LOGIN_FAILED);
        }

        log.info("User {} found in database", username);
        User user = optionalUser.get();
        if (!user.getIsActive()) {
            throw new GeneralException(HttpStatus.FORBIDDEN, USER_IS_INACTIVE);
        }

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        }

        return new UserSecurity(user.getId(), user.getUsername(), user.getPassword(), authorities);
    }

    @Override
    public void refreshToken(HttpServletRequest request,
                             HttpServletResponse response) {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
            try {
                String refreshToken = authorizationHeader.substring(BEARER.length());
                Algorithm algorithm = Algorithm.HMAC256(this.appConf.getSecretKey().getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(refreshToken);
                String username = decodedJWT.getSubject();
                User user = userDAO.findByUsername(username).orElse(null);

                String accessToken = getToken(user, TokenType.ACCESS_TOKEN);

                TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokenResponse);
            } catch (Exception exception) {
                throw new GeneralException(HttpStatus.GONE, INVALID_REFRESH_TOKEN);
            }
        } else {
            throw new GeneralException(HttpStatus.BAD_REQUEST, REFRESH_TOKEN_MISSING);
        }
    }

    @Override
    public ResponseEntity<ResetPasswordResponse> resetPassword(ResetPasswordRequest request) {
        User user = validatorService.getUserValidated(request.getUsername());

        String newPassword = request.getNewPassword();
        if (!InputValidation.isPasswordValid(newPassword)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_PASSWORD);
        }
        user.setPassword(passwordEncoder.encode(newPassword));

        ResetPasswordResponse response = new ResetPasswordResponse();
        response.setMessage(RESET_PASSWORD_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SendRegisterOTPResponse> sendRegisterOTP(SendRegisterOTPRequest request) throws JsonProcessingException {
        String phone = request.getPhone();
        if (!InputValidation.isPhoneValid(phone)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_PHONE_NUMBER);
        }

        if (userDAO.findByUsername(phone).isPresent()) {
            throw new GeneralException(HttpStatus.CONFLICT, ACCOUNT_EXISTED);
        }

        SmsRequest sms = getSmsRequest(request, OTPType.REGISTER);
        smsService.sendAndSaveOTP(sms);

        SendRegisterOTPResponse response = new SendRegisterOTPResponse();
        response.setMessage(NEW_ACCOUNT_VALID);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CFRegisterResponse> confirmRegister(CFRegisterRequest request) throws IOException {
        validateRegisterInput(request);

        User user = buildUser(request, request.getAvatar());
        userDAO.save(user);

        String roleType = request.getRoleType().name();
        addRoleToUser(user.getUsername(), roleType);

        if (ROLE_PENDING_REPAIRER.equals(RoleType.valueOf(roleType))) {
            createRepairer(user);
            createBalance(user);
        }

        saveUserAddress(user.getUsername(), request);

        String accessToken = getToken(user, TokenType.ACCESS_TOKEN);
        String refreshToken = getToken(user, TokenType.REFRESH_TOKEN);

        OTPInfo otpInfo = getOTPInfo(request, OTPType.REGISTER);
        redisDAO.deleteOTP(otpInfo);

        CFRegisterResponse response = new CFRegisterResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void createRepairer(User user) {
        Repairer repairer = new Repairer();
        repairer.setUserId(user.getId());
        repairerDAO.save(repairer);
    }

    private void createBalance(User user) {
        Balance balance = new Balance();
        balance.setUserId(user.getId());
        balance.setBalance(0L);
        balanceDAO.save(balance);
    }

    private void validateRegisterInput(CFRegisterRequest request) {
        if (!isCreatableAccount(request.getRoleType())) {
            throw new GeneralException(HttpStatus.GONE, INVALID_ROLE_TYPE);
        } else if (isNotValidOTP(request, OTPType.REGISTER)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_OTP);
        } else if (!InputValidation.isPhoneValid(request.getPhone())) {
            throw new GeneralException(HttpStatus.GONE, INVALID_PHONE_NUMBER);
        } else if (userDAO.findByUsername(request.getPhone()).isPresent()) {
            throw new GeneralException(HttpStatus.CONFLICT, ACCOUNT_EXISTED);
        } else if (!InputValidation.isPasswordValid(request.getPassword())) {
            throw new GeneralException(HttpStatus.GONE, INVALID_PASSWORD);
        } else if (cityDAO.findById(request.getCityId()).isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_CITY);
        } else if (districtDAO.findById(request.getDistrictId()).isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_DISTRICT);
        } else if (communeDAO.findById(request.getCommuneId()).isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_COMMUNE);
        } else if (!InputValidation.isFullNameValid(request.getFullName())) {
            throw new GeneralException(HttpStatus.GONE, INVALID_FULL_NAME);
        }
    }

    private boolean isCreatableAccount(RoleType roleType) {
        return ROLE_PENDING_REPAIRER.equals(roleType) || ROLE_CUSTOMER.equals(roleType);
    }

    private String getToken(User user, TokenType tokenType) {
        Algorithm algorithm = Algorithm.HMAC256(this.appConf.getSecretKey().getBytes());
        String token = Strings.EMPTY;

        switch (tokenType) {
            case ACCESS_TOKEN:
                token = JWT.create()
                        .withJWTId(String.valueOf(user.getId()))
                        .withSubject(user.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis() + this.appConf.getLifeTimeToke()))
                        .withClaim(ROLES, user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                        .sign(algorithm);
                break;
            case REFRESH_TOKEN:
                token = JWT.create()
                        .withJWTId(String.valueOf(user.getId()))
                        .withSubject(user.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis() + this.appConf.getLifeTimeRefreshToken()))
                        .sign(algorithm);
                break;
        }

        return token;
    }

    private User buildUser(CFRegisterRequest registerAccount, MultipartFile avatar) throws IOException {
        User user = new User();
        user.setFullName(registerAccount.getFullName());
        user.setPhone(registerAccount.getPhone());
        user.setIsActive(true);
        user.setUsername(registerAccount.getPhone());
        user.setPassword(passwordEncoder.encode(registerAccount.getPassword()));
        user = postUserAvatar(user, avatar);
        return user;
    }

    public User postUserAvatar(User user, MultipartFile avatar) throws IOException {
        if (avatar != null) {
            String url = cloudStorageService.uploadImage(avatar);
            user = userService.addNewAvatarToUser(user, url);
        } else {
            user.setAvatar(appConf.getDefaultAvatar());
        }
        return user;
    }

    public void saveUserAddress(String username, CFRegisterRequest registerAccount) {
        Optional<User> optionalUser = userDAO.findByUsername(username);
        Optional<Commune> optionalCommune = communeDAO.findById(registerAccount.getCommuneId());

        if (optionalUser.isPresent() && optionalCommune.isPresent()) {
            User user = optionalUser.get();
            Commune commune = optionalCommune.get();

            UserAddress userAddress = new UserAddress();
            userAddress.setUserId(user.getId());
            userAddress.setMainAddress(true);
            userAddress.setStreetAddress(registerAccount.getStreetAddress());
            userAddress.setName(registerAccount.getFullName());
            userAddress.setPhone(registerAccount.getPhone());
            userAddress.setCommuneId(commune.getId());
            userAddressDAO.save(userAddress);
        }
    }

    private void addRoleToUser(String username, String roleName) {
        log.info("adding role {} to user {}", roleName, username);
        Optional<User> optionalUser = userDAO.findByUsername(username);
        Optional<Role> optionalRole = roleDAO.findByName(roleName);

        if (optionalUser.isPresent() && optionalRole.isPresent()) {
            User user = optionalUser.get();
            Role role = optionalRole.get();
            user.getRoles().add(role);
        }
    }

    @Override
    public ResponseEntity<SendForgotPassOTPResponse> sendForgotPassOTP(SendForgotPassOTPRequest request) throws JsonProcessingException {
        String phone = request.getPhone();
        if (!InputValidation.isPhoneValid(phone)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_PHONE_NUMBER);
        }

        validatorService.getUserValidated(phone);

        SmsRequest sms = getSmsRequest(request, OTPType.FORGOT_PASSWORD);
        smsService.sendAndSaveOTP(sms);

        SendForgotPassOTPResponse response = new SendForgotPassOTPResponse();
        response.setMessage(SEND_FORGOT_PASSWORD_OTP_SUCCESS);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private SmsRequest getSmsRequest(PhoneDTO phoneDTO, OTPType otpType) {
        SmsRequest sms = new SmsRequest();
        sms.setUsername(phoneDTO.getPhone());
        sms.setPhoneNumberFormatted(PhoneFormatter.getVietNamePhoneNumber(phoneDTO.getPhone()));
        sms.setOtpType(otpType);
        return sms;
    }

    @Override
    public ResponseEntity<CFForgotPassResponse> confirmForgotPassword(CFForgotPassRequest request) {
        String phone = request.getPhone();
        if (isNotValidOTP(request, OTPType.FORGOT_PASSWORD)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_OTP);
        } else if (!InputValidation.isPhoneValid(phone)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_PHONE_NUMBER);
        }

        User user = validatorService.getUserValidated(phone);
        String accessToken = getToken(user, TokenType.ACCESS_TOKEN);

        OTPInfo otpInfo = getOTPInfo(request, OTPType.FORGOT_PASSWORD);
        redisDAO.deleteOTP(otpInfo);

        CFForgotPassResponse response = new CFForgotPassResponse();
        response.setAccessToken(accessToken);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private OTPInfo getOTPInfo(OTPRequest request, OTPType otpType) {
        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setOtp(request.getOtp());
        otpInfo.setUsername(request.getPhone());
        otpInfo.setOtpType(otpType);
        return otpInfo;
    }

    private boolean isNotValidOTP(OTPRequest request, OTPType otpType) {
        OTPInfo otpInfo = redisDAO.findOTP(request, otpType);
        return otpInfo == null;
    }
}
