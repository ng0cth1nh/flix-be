package com.fpt.flix.flix_app.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.flix.flix_app.configurations.AppConf;
import com.fpt.flix.flix_app.models.db.OTPInfo;
import com.fpt.flix.flix_app.models.db.Role;
import com.fpt.flix.flix_app.models.db.User;
import com.fpt.flix.flix_app.models.errors.GeneralException;
import com.fpt.flix.flix_app.models.requests.CFRegisterCustomerRequest;
import com.fpt.flix.flix_app.models.requests.RegisterCustomerRequest;
import com.fpt.flix.flix_app.models.requests.SmsRequest;
import com.fpt.flix.flix_app.models.responses.CFRegisterCustomerResponse;
import com.fpt.flix.flix_app.models.responses.RegisterCustomerResponse;
import com.fpt.flix.flix_app.models.responses.TokenResponse;
import com.fpt.flix.flix_app.repositories.RedisRepository;
import com.fpt.flix.flix_app.repositories.RoleRepository;
import com.fpt.flix.flix_app.repositories.UserRepository;
import com.fpt.flix.flix_app.utils.InputValidation;
import com.fpt.flix.flix_app.utils.PhoneFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.fpt.flix.flix_app.constants.Constant.*;
import static com.fpt.flix.flix_app.constants.RoleType.ROLE_CUSTOMER;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
@Transactional
@Slf4j
public class CustomerService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppConf appConf;
    private final RedisRepository redisRepository;
    private final SmsService smsService;


    public CustomerService(UserRepository userRepository,
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
                User user = getUser(username);

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

    public User getUser(String username) {
        log.info("fetching user {}", username);
        return userRepository.findByUsername(username).orElse(null);
    }

    public ResponseEntity<RegisterCustomerResponse> registerCustomer(RegisterCustomerRequest request) throws JsonProcessingException {
        if (!InputValidation.isPhoneValid(request.getPhone())) {
            throw new GeneralException(INVALID_PHONE_NUMBER);
        }

        if (userRepository.findByUsername(request.getPhone()).isPresent()) {
            throw new GeneralException(ACCOUNT_EXISTED);
        }

        if (!InputValidation.isPasswordValid(request.getPassword())) {
            throw new GeneralException(INVALID_PASSWORD);
        }

        request.setPassword(passwordEncoder.encode(request.getPassword()));
        redisRepository.saveRegisterAccount(request);

        SmsRequest sms = new SmsRequest();
        sms.setUsername(request.getPhone());
        sms.setPhoneNumber(PhoneFormatter.getVietNamePhoneNumber(request.getPhone()));
        smsService.sendAndSaveOTP(sms);

        RegisterCustomerResponse response = new RegisterCustomerResponse();
        response.setMessage(NEW_ACCOUNT_VALID);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<CFRegisterCustomerResponse> confirmRegisterCustomer(CFRegisterCustomerRequest request) {
        OTPInfo otpInfo = redisRepository.findOTP(request);
        if (otpInfo == null) {
            throw new GeneralException(INVALID_OTP);
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new GeneralException(ACCOUNT_EXISTED);
        }

        RegisterCustomerRequest registerAccount = redisRepository.findRegisterAccount(request.getUsername());

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setFullName(registerAccount.getFirstName() + " " + registerAccount.getLastName());
        user.setFirstName(registerAccount.getFirstName());
        user.setLastName(registerAccount.getLastName());
        user.setPhone(registerAccount.getPhone());
        user.setIsActive(true);
        user.setUsername(registerAccount.getPhone());
        user.setPassword(registerAccount.getPassword());
        addRoleToUser(user.getUsername(), ROLE_CUSTOMER.name());
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userRepository.save(user);

        Algorithm algorithm = Algorithm.HMAC256(this.appConf.getSecretKey().getBytes());
        String accessToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + this.appConf.getLifeTimeToke()))
                .withClaim(ROLES, user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .sign(algorithm);

        String refreshToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + this.appConf.getLifeTimeRefreshToken()))
                .sign(algorithm);

        CFRegisterCustomerResponse response = new CFRegisterCustomerResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    public Role saveRole(Role role) {
        log.info("Saving new role {} to the database", role.getName());
        return roleRepository.save(role);
    }

    public User saveUser(User user) {
        log.info("Saving new user {} to the database", user.getUsername());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
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
