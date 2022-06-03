package com.fpt.flix.flix_app.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.flix.flix_app.configurations.AppConf;
import com.fpt.flix.flix_app.constants.enums.TokenType;
import com.fpt.flix.flix_app.models.db.OTPInfo;
import com.fpt.flix.flix_app.models.db.Role;
import com.fpt.flix.flix_app.models.db.User;
import com.fpt.flix.flix_app.models.errors.GeneralException;
import com.fpt.flix.flix_app.models.requests.*;
import com.fpt.flix.flix_app.models.responses.*;
import com.fpt.flix.flix_app.repositories.RedisRepository;
import com.fpt.flix.flix_app.repositories.RoleRepository;
import com.fpt.flix.flix_app.repositories.UserRepository;
import com.fpt.flix.flix_app.utils.InputValidation;
import com.fpt.flix.flix_app.utils.PhoneFormatter;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.fpt.flix.flix_app.constants.Constant.*;
import static com.fpt.flix.flix_app.constants.enums.RoleType.ROLE_CUSTOMER;
import static com.fpt.flix.flix_app.constants.enums.RoleType.ROLE_PENDING_REPAIRER;
import static com.fpt.flix.flix_app.constants.enums.TokenType.ACCESS_TOKEN;
import static com.fpt.flix.flix_app.constants.enums.TokenType.REFRESH_TOKEN;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
@Transactional
@Slf4j
public class AccountService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppConf appConf;
    private final RedisRepository redisRepository;
    private final SmsService smsService;


    public AccountService(UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          AppConf appConf,
                          RedisRepository redisRepository,
                          SmsService smsService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.appConf = appConf;
        this.redisRepository = redisRepository;
        this.smsService = smsService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findByUsername(username);
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
                User user = userRepository.findByUsername(username).orElse(null);

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

    public ResponseEntity<RegisterCustomerResponse> registerCustomer(RegisterCustomerRequest request) throws JsonProcessingException {
        validateRegisterInput(request);

        request.setPassword(passwordEncoder.encode(request.getPassword()));
        redisRepository.saveRegisterAccount(request);

        SmsRequest sms = getSmsRequest(request);
        smsService.sendAndSaveOTP(sms);

        RegisterCustomerResponse response = new RegisterCustomerResponse();
        response.setMessage(NEW_ACCOUNT_VALID);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<RegisterRepairerResponse> registerRepairer(RegisterRepairerRequest request) throws JsonProcessingException {
        validateRegisterInput(request);

        request.setPassword(passwordEncoder.encode(request.getPassword()));
        redisRepository.saveRegisterAccount(request);

        SmsRequest sms = getSmsRequest(request);
        smsService.sendAndSaveOTP(sms);

        RegisterRepairerResponse response = new RegisterRepairerResponse();
        response.setMessage(NEW_ACCOUNT_VALID);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public void validateRegisterInput(RegisterRequest request) {
        if (!InputValidation.isPhoneValid(request.getPhone())) {
            throw new GeneralException(INVALID_PHONE_NUMBER);
        }

        if (userRepository.findByUsername(request.getPhone()).isPresent()) {
            throw new GeneralException(ACCOUNT_EXISTED);
        }

        if (!InputValidation.isPasswordValid(request.getPassword())) {
            throw new GeneralException(INVALID_PASSWORD);
        }
    }

    public SmsRequest getSmsRequest(RegisterRequest registerRequest) {
        SmsRequest sms = new SmsRequest();
        sms.setUsername(registerRequest.getPhone());
        sms.setPhoneNumber(PhoneFormatter.getVietNamePhoneNumber(registerRequest.getPhone()));
        return sms;
    }

    public ResponseEntity<CFRegisterCustomerResponse> confirmRegisterCustomer(CFRegisterCustomerRequest request) {
        validateOTP(request);

        RegisterRequest registerAccount = redisRepository.findRegisterAccount(request.getUsername());

        User user = buildUser(registerAccount);
        userRepository.save(user);
        addRoleToUser(user.getUsername(), ROLE_CUSTOMER.name());

        String accessToken = getToken(user, ACCESS_TOKEN);
        String refreshToken = getToken(user, REFRESH_TOKEN);

        CFRegisterCustomerResponse response = new CFRegisterCustomerResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<CFRegisterRepairerResponse> confirmRegisterRepairer(CFRegisterRepairerRequest request) {
        validateOTP(request);

        RegisterRequest registerAccount = redisRepository.findRegisterAccount(request.getUsername());

        User user = buildUser(registerAccount);
        userRepository.save(user);
        addRoleToUser(user.getUsername(), ROLE_PENDING_REPAIRER.name());

        String accessToken = getToken(user, ACCESS_TOKEN);
        String refreshToken = getToken(user, REFRESH_TOKEN);

        CFRegisterRepairerResponse response = new CFRegisterRepairerResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);

        return new ResponseEntity<>(response, HttpStatus.OK);
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

    public void validateOTP(OTPRequest request) {
        OTPInfo otpInfo = redisRepository.findOTP(request);
        if (otpInfo == null) {
            throw new GeneralException(INVALID_OTP);
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new GeneralException(ACCOUNT_EXISTED);
        }
    }

    public User buildUser(RegisterRequest registerAccount) {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setFullName(registerAccount.getFirstName() + " " + registerAccount.getLastName());
        user.setFirstName(registerAccount.getFirstName());
        user.setLastName(registerAccount.getLastName());
        user.setPhone(registerAccount.getPhone());
        user.setIsActive(true);
        user.setUsername(registerAccount.getPhone());
        user.setPassword(registerAccount.getPassword());
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return user;
    }

    public void addRoleToUser(String username, String roleName) {
        log.info("adding role {} to user {}", roleName, username);
        Optional<User> optionalUser = userRepository.findByUsername(username);
        Optional<Role> optionalRole = roleRepository.findByName(roleName);

        if (optionalUser.isPresent() && optionalRole.isPresent()) {
            User user = optionalUser.get();
            Role role = optionalRole.get();
            user.getRoles().add(role);
        }
    }
}
