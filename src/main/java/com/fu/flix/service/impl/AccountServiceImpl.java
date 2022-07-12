package com.fu.flix.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.enums.IdentityCardType;
import com.fu.flix.constant.enums.OTPType;
import com.fu.flix.constant.enums.TokenType;
import com.fu.flix.dao.*;
import com.fu.flix.dto.PhoneDTO;
import com.fu.flix.dto.security.UserSecurity;
import com.fu.flix.entity.*;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.response.*;
import com.fu.flix.service.*;
import com.fu.flix.util.DateFormatUtil;
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
import org.springframework.util.CollectionUtils;
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
    private final UserAddressDAO userAddressDAO;
    private final PasswordEncoder passwordEncoder;
    private final CloudStorageService cloudStorageService;
    private final UserService userService;
    private final RepairerDAO repairerDAO;
    private final BalanceDAO balanceDAO;
    private final ValidatorService validatorService;
    private final Long NAME_MAX_LENGTH;
    private final Long DESCRIPTION_MAX_LENGTH;
    private final IdentityCardDAO identityCardDAO;
    private final ImageDAO imageDAO;
    private final CertificateDAO certificateDAO;
    private final String DATE_PATTERN = "dd-MM-yyyy";

    public AccountServiceImpl(UserDAO userDAO,
                              RoleDAO roleDAO,
                              AppConf appConf,
                              RedisDAO redisDAO,
                              SmsService smsService,
                              CommuneDAO communeDAO,
                              UserAddressDAO userAddressDAO,
                              PasswordEncoder passwordEncoder,
                              CloudStorageService cloudStorageService,
                              UserService userService,
                              RepairerDAO repairerDAO,
                              BalanceDAO balanceDAO,
                              ValidatorService validatorService,
                              IdentityCardDAO identityCardDAO,
                              ImageDAO imageDAO,
                              CertificateDAO certificateDAO) {
        this.userDAO = userDAO;
        this.roleDAO = roleDAO;
        this.appConf = appConf;
        this.redisDAO = redisDAO;
        this.smsService = smsService;
        this.communeDAO = communeDAO;
        this.userAddressDAO = userAddressDAO;
        this.passwordEncoder = passwordEncoder;
        this.cloudStorageService = cloudStorageService;
        this.userService = userService;
        this.repairerDAO = repairerDAO;
        this.balanceDAO = balanceDAO;
        this.validatorService = validatorService;
        this.NAME_MAX_LENGTH = appConf.getNameMaxLength();
        this.DESCRIPTION_MAX_LENGTH = appConf.getDescriptionMaxLength();
        this.identityCardDAO = identityCardDAO;
        this.imageDAO = imageDAO;
        this.certificateDAO = certificateDAO;
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
                User user = validatorService.getUserValidated(username);

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
    public ResponseEntity<CFRegisterCustomerResponse> confirmRegisterCustomer(CFRegisterCustomerRequest request) throws IOException {
        validateRegisterCustomerInput(request);

        User user = buildCustomerUser(request, request.getAvatar());
        userDAO.save(user);

        addRoleToUser(user.getUsername(), ROLE_CUSTOMER.name());
        saveCustomerUserAddress(user.getUsername(), request);

        String accessToken = getToken(user, TokenType.ACCESS_TOKEN);
        String refreshToken = getToken(user, TokenType.REFRESH_TOKEN);

        OTPInfo otpInfo = getOTPInfo(request, OTPType.REGISTER);
        redisDAO.deleteOTP(otpInfo);

        CFRegisterCustomerResponse response = new CFRegisterCustomerResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setMessage(CONFIRM_REGISTER_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CFRegisterRepairerResponse> confirmRegisterRepairer(CFRegisterRepairerRequest request) throws IOException {
        validateRegisterRepairerInput(request);

        User user = buildRepairerUser(request, request.getAvatar());
        userDAO.save(user);

        addRoleToUser(user.getUsername(), ROLE_PENDING_REPAIRER.name());
        saveRepairerUserAddress(user.getUsername(), request);

        createRepairer(user, request);
        createBalance(user);
        createIdentityCard(user, request);
        createCertificates(user, request.getCertificates());

        String accessToken = getToken(user, TokenType.ACCESS_TOKEN);
        String refreshToken = getToken(user, TokenType.REFRESH_TOKEN);

        OTPInfo otpInfo = getOTPInfo(request, OTPType.REGISTER);
        redisDAO.deleteOTP(otpInfo);

        CFRegisterRepairerResponse response = new CFRegisterRepairerResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setMessage(CONFIRM_REGISTER_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void validateRegisterCustomerInput(CFRegisterCustomerRequest request) {
        if (!InputValidation.isPhoneValid(request.getPhone())) {
            throw new GeneralException(HttpStatus.GONE, INVALID_PHONE_NUMBER);
        } else if (userDAO.findByUsername(request.getPhone()).isPresent()) {
            throw new GeneralException(HttpStatus.CONFLICT, ACCOUNT_EXISTED);
        } else if (!InputValidation.isPasswordValid(request.getPassword())) {
            throw new GeneralException(HttpStatus.GONE, INVALID_PASSWORD);
        } else if (isInvalidCommune(request.getCommuneId())) {
            throw new GeneralException(HttpStatus.GONE, INVALID_COMMUNE);
        } else if (!InputValidation.isNameValid(request.getFullName(), NAME_MAX_LENGTH)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_FULL_NAME);
        } else if (isInvalidStreetAddress(request.getStreetAddress())) {
            throw new GeneralException(HttpStatus.GONE, INVALID_STREET_ADDRESS);
        } else if (isNotValidOTP(request, OTPType.REGISTER)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_OTP);
        }
    }

    private User buildCustomerUser(CFRegisterCustomerRequest registerAccount, MultipartFile avatar) throws IOException {
        User user = new User();
        user.setFullName(registerAccount.getFullName());
        user.setPhone(registerAccount.getPhone());
        user.setIsActive(true);
        user.setUsername(registerAccount.getPhone());
        user.setPassword(passwordEncoder.encode(registerAccount.getPassword()));
        user = postUserAvatar(user, avatar);
        return user;
    }

    private void validateRegisterRepairerInput(CFRegisterRepairerRequest request) {
        if (!InputValidation.isPhoneValid(request.getPhone())) {
            throw new GeneralException(HttpStatus.GONE, INVALID_PHONE_NUMBER);
        } else if (userDAO.findByUsername(request.getPhone()).isPresent()) {
            throw new GeneralException(HttpStatus.CONFLICT, ACCOUNT_EXISTED);
        } else if (!InputValidation.isPasswordValid(request.getPassword())) {
            throw new GeneralException(HttpStatus.GONE, INVALID_PASSWORD);
        } else if (isInvalidCommune(request.getCommuneId())) {
            throw new GeneralException(HttpStatus.GONE, INVALID_COMMUNE);
        } else if (!InputValidation.isNameValid(request.getFullName(), NAME_MAX_LENGTH)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_FULL_NAME);
        } else if (isInvalidStreetAddress(request.getStreetAddress())) {
            throw new GeneralException(HttpStatus.GONE, INVALID_STREET_ADDRESS);
        } else if (isInvalidExperienceDescription(request.getExperienceDescription())) {
            throw new GeneralException(HttpStatus.GONE, INVALID_EXPERIENCE_DESCRIPTION);
        } else if (isEmptyCertificates(request.getCertificates())) {
            throw new GeneralException(HttpStatus.GONE, CERTIFICATE_IS_REQUIRED);
        } else if (isInvalidCertificates(request.getCertificates())) {
            throw new GeneralException(HttpStatus.GONE, CERTIFICATE_FILE_MUST_BE_IMAGE_OR_PDF);
        } else if (isInvalidExperienceYears(request.getExperienceYear())) {
            throw new GeneralException(HttpStatus.GONE, INVALID_EXPERIENCE_YEARS);
        } else if (isInvalidImage(request.getFrontImage())) {
            throw new GeneralException(HttpStatus.GONE, FRONT_IMAGE_MUST_BE_IMAGE_OR_PDF);
        } else if (isInvalidImage(request.getBackSideImage())) {
            throw new GeneralException(HttpStatus.GONE, BACK_SIDE_IMAGE_MUST_BE_IMAGE_OR_PDF);
        } else if (!InputValidation.isIdentityCardNumberValid(request.getIdentityCardNumber())) {
            throw new GeneralException(HttpStatus.GONE, INVALID_IDENTITY_CARD_NUMBER);
        } else if (isInvalidIdentityCardType(request.getIdentityCardType())) {
            throw new GeneralException(HttpStatus.GONE, IDENTITY_CARD_TYPE_MUST_BE_CCCD_OR_CMND);
        } else if (request.getGender() == null) {
            throw new GeneralException(HttpStatus.GONE, GENDER_IS_REQUIRED);
        } else if (!InputValidation.isValidDate(request.getDateOfBirth(), DATE_PATTERN)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_DATE_OF_BIRTH);
        } else if (isIdentityCardExisted(request.getIdentityCardNumber())) {
            throw new GeneralException(HttpStatus.GONE, IDENTITY_CARD_NUMBER_EXISTED);
        } else if (isNotValidOTP(request, OTPType.REGISTER)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_OTP);
        }
    }

    private boolean isInvalidCommune(String communeId) {
        if (Strings.isEmpty(communeId)) {
            return true;
        }
        Optional<Commune> optionalCommune = communeDAO.findById(communeId);
        return optionalCommune.isEmpty();
    }

    private boolean isInvalidStreetAddress(String streetAddress) {
        return Strings.isEmpty(streetAddress) || streetAddress.length() > NAME_MAX_LENGTH;
    }

    private boolean isInvalidExperienceDescription(String experienceDescription) {
        return Strings.isEmpty(experienceDescription) || experienceDescription.length() > DESCRIPTION_MAX_LENGTH;
    }

    private boolean isEmptyCertificates(List<MultipartFile> certificates) {
        return CollectionUtils.isEmpty(certificates);
    }

    private boolean isInvalidCertificates(List<MultipartFile> certificates) {
        for (MultipartFile file : certificates) {
            if (!cloudStorageService.isCertificateFile(file)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInvalidExperienceYears(Integer experienceYear) {
        return experienceYear == null || experienceYear < 0;
    }

    private boolean isInvalidImage(MultipartFile file) {
        return file == null || !cloudStorageService.isImageFile(file);
    }

    private boolean isInvalidIdentityCardType(String type) {
        for (IdentityCardType t : IdentityCardType.values()) {
            if (t.name().equals(type)) {
                return false;
            }
        }
        return true;
    }

    private boolean isIdentityCardExisted(String card) {
        return identityCardDAO.findByIdentityCardNumber(card).isPresent();
    }

    private User buildRepairerUser(CFRegisterRepairerRequest registerAccount, MultipartFile avatar) throws IOException {
        User user = new User();
        user.setFullName(registerAccount.getFullName());
        user.setPhone(registerAccount.getPhone());
        user.setIsActive(true);
        user.setUsername(registerAccount.getPhone());
        user.setPassword(passwordEncoder.encode(registerAccount.getPassword()));
        user.setGender(registerAccount.getGender());
        user.setDateOfBirth(DateFormatUtil.getLocalDate(registerAccount.getDateOfBirth(), DATE_PATTERN));
        user = postUserAvatar(user, avatar);
        return user;
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

    public void saveCustomerUserAddress(String username, CFRegisterCustomerRequest registerAccount) {
        saveUserAddress(username,
                registerAccount.getCommuneId(),
                registerAccount.getStreetAddress(),
                registerAccount.getFullName(),
                registerAccount.getPhone()
        );
    }

    public void saveRepairerUserAddress(String username, CFRegisterRepairerRequest registerAccount) {
        saveUserAddress(username,
                registerAccount.getCommuneId(),
                registerAccount.getStreetAddress(),
                registerAccount.getFullName(),
                registerAccount.getPhone()
        );
    }

    private void createRepairer(User user, CFRegisterRepairerRequest request) {
        Repairer repairer = new Repairer();
        repairer.setUserId(user.getId());
        repairer.setExperienceDescription(request.getExperienceDescription());
        repairer.setExperienceYear(request.getExperienceYear());
        repairerDAO.save(repairer);
    }

    private void createBalance(User user) {
        Balance balance = new Balance();
        balance.setUserId(user.getId());
        balance.setBalance(0L);
        balanceDAO.save(balance);
    }

    private void createIdentityCard(User user, CFRegisterRepairerRequest request) throws IOException {
        IdentityCard identityCard = new IdentityCard();
        identityCard.setIdentityCardNumber(request.getIdentityCardNumber());
        identityCard.setType(request.getIdentityCardType());
        identityCard.setRepairerId(user.getId());
        identityCard = postFrontIdentityImage(identityCard, request.getFrontImage());
        identityCard = postBackSideIdentityImage(identityCard, request.getBackSideImage());
        identityCardDAO.save(identityCard);
    }

    private void createCertificates(User user, List<MultipartFile> certificateFiles) throws IOException {
        List<Certificate> certificates = new ArrayList<>();

        for (MultipartFile file : certificateFiles) {
            String url = cloudStorageService.uploadCertificateFile(file);
            Certificate certificate = new Certificate();
            certificate.setRepairerId(user.getId());
            certificate.setUrl(url);
            certificates.add(certificate);
        }

        certificateDAO.saveAll(certificates);
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


    private User postUserAvatar(User user, MultipartFile avatar) throws IOException {
        if (avatar != null) {
            String url = cloudStorageService.uploadImage(avatar);
            user = userService.addNewAvatarToUser(user, url);
        } else {
            user.setAvatar(appConf.getDefaultAvatar());
        }
        return user;
    }

    public IdentityCard postFrontIdentityImage(IdentityCard identityCard, MultipartFile imageFile) throws IOException {
        String url = cloudStorageService.uploadImage(imageFile);

        Image image = new Image();
        image.setName(FRONT_IMAGE);
        image.setUrl(url);
        Image savedImage = imageDAO.save(image);
        identityCard.setFrontImageId(savedImage.getId());
        return identityCard;
    }

    public IdentityCard postBackSideIdentityImage(IdentityCard identityCard, MultipartFile imageFile) throws IOException {
        String url = cloudStorageService.uploadImage(imageFile);

        Image image = new Image();
        image.setName(BACK_SIDE_IMAGE);
        image.setUrl(url);
        Image savedImage = imageDAO.save(image);
        identityCard.setBackSideImageId(savedImage.getId());
        return identityCard;
    }


    private void saveUserAddress(String username, String communeId, String streetAddress, String fullName, String phone) {
        Optional<User> optionalUser = userDAO.findByUsername(username);
        Optional<Commune> optionalCommune = communeDAO.findById(communeId);

        if (optionalUser.isPresent() && optionalCommune.isPresent()) {
            User user = optionalUser.get();
            Commune commune = optionalCommune.get();

            UserAddress userAddress = new UserAddress();
            userAddress.setUserId(user.getId());
            userAddress.setMainAddress(true);
            userAddress.setStreetAddress(streetAddress);
            userAddress.setName(fullName);
            userAddress.setPhone(phone);
            userAddress.setCommuneId(commune.getId());
            userAddressDAO.save(userAddress);
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
        if (!InputValidation.isPhoneValid(phone)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_PHONE_NUMBER);
        }

        if (isNotValidOTP(request, OTPType.FORGOT_PASSWORD)) {
            throw new GeneralException(HttpStatus.GONE, INVALID_OTP);
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
