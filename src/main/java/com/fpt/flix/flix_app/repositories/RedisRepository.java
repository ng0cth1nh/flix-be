package com.fpt.flix.flix_app.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.flix.flix_app.constants.enums.RedisDataTypeEnum;
import com.fpt.flix.flix_app.models.requests.OTPRequest;
import com.fpt.flix.flix_app.models.db.OTPInfo;
import com.fpt.flix.flix_app.models.requests.RegisterCustomerRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;

import static com.fpt.flix.flix_app.constants.Constant.REDIS_KEY_OTP_PREFIX;
import static com.fpt.flix.flix_app.constants.Constant.REDIS_KEY_REGISTER_PREFIX;

@Service
public class RedisRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;


    @Autowired
    public RedisRepository(RedisTemplate<String, String> redisTemplate,
                           ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public OTPInfo findOTP(OTPRequest OTPRequest) {
        String key = REDIS_KEY_OTP_PREFIX + OTPRequest.getUsername() + "_" + OTPRequest.getOtp();
        OTPInfo otpInfo = null;
        try {
            Object data = redisTemplate.opsForValue().get(key);
            otpInfo = stringToObject(String.valueOf(data), OTPInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return otpInfo;
    }


    public void saveOTP(OTPInfo otpInfo) throws JsonProcessingException {
        String key = REDIS_KEY_OTP_PREFIX + otpInfo.getUsername() + "_" + otpInfo.getOtp();
        redisTemplate.opsForValue().set(key, objectToString(otpInfo));
        redisTemplate.expireAt(key, Instant.now().plusSeconds(60));
    }

    public void saveRegisterAccount(RegisterCustomerRequest request) throws JsonProcessingException {
        String key = REDIS_KEY_REGISTER_PREFIX + "_" + request.getPhone();
        redisTemplate.opsForValue().set(key, objectToString(request));
        redisTemplate.expireAt(key, Instant.now().plusSeconds(60));
    }

    public RegisterCustomerRequest findRegisterAccount(String phoneNumber) {
        String key = REDIS_KEY_REGISTER_PREFIX + "_" + phoneNumber;
        RegisterCustomerRequest account = null;
        try {
            Object data = redisTemplate.opsForValue().get(key);
            account = stringToObject(String.valueOf(data), RegisterCustomerRequest.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return account;
    }

    private <E> E stringToObject(String data, Class<E> clazz) throws IOException {
        if ("null".equals(data)) {
            return null;
        }
        String type = data.substring(0, 1);
        if (type.equals(RedisDataTypeEnum.NULL.getType())) {
            return null;
        }
        String value = data.substring(1);
        if (type.equals(RedisDataTypeEnum.BOOLEAN.getType())) {
            return (E) Boolean.valueOf(value);
        } else if (type.equals(RedisDataTypeEnum.STRING.getType())) {
            return (E) value;
        } else if (type.equals(RedisDataTypeEnum.NUMBER.getType())) {
            return (E) Double.valueOf(value);
        } else {
            return objectMapper.readValue(value, clazz);
        }
    }

    private <E> String objectToString(E data) throws JsonProcessingException {
        if (data == null) {
            return RedisDataTypeEnum.NULL.getType();
        } else if (data instanceof Boolean) {
            return RedisDataTypeEnum.BOOLEAN.getType() + data;
        } else if (data instanceof String) {
            return RedisDataTypeEnum.STRING.getType() + data;
        } else if (data instanceof Number) {
            return RedisDataTypeEnum.NUMBER.getType() + data;
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            return RedisDataTypeEnum.OBJECT.getType() + objectMapper.writeValueAsString(data);
        }
    }
}
