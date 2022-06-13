package com.fu.flix.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.enums.TokenType;
import com.fu.flix.dao.*;
import com.fu.flix.entity.*;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.response.*;
import com.fu.flix.service.AccountService;
import com.fu.flix.service.CloudStorageService;
import com.fu.flix.service.SmsService;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.fu.flix.constant.Constant.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
@Transactional
@Slf4j
public class AccountServiceImpl implements UserDetailsService, AccountService {
    private final UserDAO userDAO;
    private final RoleDAO roleDAO;
    private final AppConf appConf;
    private final RedisDAO redisDAO;
    private final SmsService smsService;
    private final ImageDAO imageDAO;
    private final CommuneDAO communeDAO;
    private final DistrictDAO districtDAO;
    private final CityDAO cityDAO;
    private final UserAddressDAO userAddressDAO;
    private final PasswordEncoder passwordEncoder;
    private final CloudStorageService cloudStorageService;


    public AccountServiceImpl(UserDAO userDAO,
                              RoleDAO roleDAO,
                              AppConf appConf,
                              RedisDAO redisDAO,
                              SmsService smsService,
                              ImageDAO imageDAO,
                              CommuneDAO communeDAO,
                              DistrictDAO districtDAO,
                              CityDAO cityDAO,
                              UserAddressDAO userAddressDAO,
                              PasswordEncoder passwordEncoder,
                              CloudStorageService cloudStorageService) {
        this.userDAO = userDAO;
        this.roleDAO = roleDAO;
        this.appConf = appConf;
        this.redisDAO = redisDAO;
        this.smsService = smsService;
        this.imageDAO = imageDAO;
        this.communeDAO = communeDAO;
        this.districtDAO = districtDAO;
        this.cityDAO = cityDAO;
        this.userAddressDAO = userAddressDAO;
        this.passwordEncoder = passwordEncoder;
        this.cloudStorageService = cloudStorageService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userDAO.findByUsername(username);
        if (optionalUser.isEmpty()) {
            log.error("User {} not found", username);
            throw new UsernameNotFoundException("User not found in database");
        }

        log.info("User {} found in database", username);
        User user = optionalUser.get();

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        }

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
    }

    @Override
    public void refreshToken(HttpServletRequest request,
                             HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
            try {
                String refreshToken = authorizationHeader.substring(BEARER.length());
                Algorithm algorithm = Algorithm.HMAC256(this.appConf.getSecretKey().getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(refreshToken);
                String username = decodedJWT.getSubject();
                User user = userDAO.findByUsername(username).orElse(null);

                String accessToken = JWT.create()
                        .withSubject(user.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis() + this.appConf.getLifeTimeToke()))
                        .withClaim(ROLES, user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                        .sign(algorithm);

                TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokenResponse);
            } catch (Exception exception) {

                response.setStatus(FORBIDDEN.value());
                Map<String, String> errors = new HashMap<>();
                errors.put("message", INVALID_REFRESH_TOKEN);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), errors);
            }
        } else {
            throw new GeneralException(REFRESH_TOKEN_MISSING);
        }
    }

    @Override
    public ResponseEntity<CheckUsernameResponse> sendRegisterOTP(CheckUsernameRequest request) throws JsonProcessingException {
        if (userDAO.findByUsername(request.getPhone()).isPresent()) {
            throw new GeneralException(ACCOUNT_EXISTED);
        }

        SmsRequest sms = getSmsRequest(request);
        smsService.sendAndSaveOTP(sms);

        CheckUsernameResponse response = new CheckUsernameResponse();
        response.setMessage(NEW_ACCOUNT_VALID);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private SmsRequest getSmsRequest(CheckUsernameRequest checkUsernameRequest) {
        SmsRequest sms = new SmsRequest();
        sms.setUsername(checkUsernameRequest.getPhone());
        sms.setPhoneNumber(PhoneFormatter.getVietNamePhoneNumber(checkUsernameRequest.getPhone()));
        return sms;
    }

    @Override
    public ResponseEntity<CFRegisterResponse> confirmRegister(CFRegisterRequest request) throws IOException {
        validateRegisterInput(request);

        User user = buildUser(request, request.getAvatar());
        userDAO.save(user);
        addRoleToUser(user.getUsername(), request.getRoleType().name());
        saveUserAddress(user.getUsername(), request);

        String accessToken = getToken(user, TokenType.ACCESS_TOKEN);
        String refreshToken = getToken(user, TokenType.REFRESH_TOKEN);

        CFRegisterResponse response = new CFRegisterResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void validateRegisterInput(CFRegisterRequest request) {
        if (!isValidOTP(request)) {
            throw new GeneralException(INVALID_OTP);
        } else if (!InputValidation.isPhoneValid(request.getPhone())) {
            throw new GeneralException(INVALID_PHONE_NUMBER);
        } else if (userDAO.findByUsername(request.getPhone()).isPresent()) {
            throw new GeneralException(ACCOUNT_EXISTED);
        } else if (!InputValidation.isPasswordValid(request.getPassword())) {
            throw new GeneralException(INVALID_PASSWORD);
        } else if (cityDAO.findById(request.getCityId()).isEmpty()) {
            throw new GeneralException(INVALID_CITY);
        } else if (districtDAO.findById(request.getDistrictId()).isEmpty()) {
            throw new GeneralException(INVALID_DISTRICT);
        } else if (communeDAO.findById(request.getCommuneId()).isEmpty()) {
            throw new GeneralException(INVALID_COMMUNE);
        }
    }

    private boolean isValidOTP(OTPRequest request) {
        OTPInfo otpInfo = redisDAO.findOTP(request);
        return otpInfo != null;
    }

    private String getToken(User user, TokenType tokenType) {
        Algorithm algorithm = Algorithm.HMAC256(this.appConf.getSecretKey().getBytes());
        String token = Strings.EMPTY;

        switch (tokenType) {
            case ACCESS_TOKEN:
                token = JWT.create()
                        .withSubject(user.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis() + this.appConf.getLifeTimeToke()))
                        .withClaim(ROLES, user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                        .sign(algorithm);
                break;
            case REFRESH_TOKEN:
                token = JWT.create()
                        .withSubject(user.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis() + this.appConf.getLifeTimeRefreshToken()))
                        .sign(algorithm);
                break;
        }

        return token;
    }

    private User buildUser(CFRegisterRequest registerAccount, MultipartFile avatar) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setFullName(registerAccount.getFullName());
        user.setPhone(registerAccount.getPhone());
        user.setIsActive(true);
        user.setUsername(registerAccount.getPhone());
        user.setPassword(passwordEncoder.encode(registerAccount.getPassword()));
        user = updateUserAvatar(user, avatar);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return user;
    }

    public User updateUserAvatar(User user, MultipartFile avatar) throws IOException {
        if (avatar != null) {
            String url = cloudStorageService.uploadImage(avatar);
            Image image = new Image();
            image.setName(user.getFullName());
            image.setUrl(url);
            Image savedImage = imageDAO.save(image);
            user.setAvatar(savedImage.getId());
        } else {
            user.setAvatar(1L);
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
}
