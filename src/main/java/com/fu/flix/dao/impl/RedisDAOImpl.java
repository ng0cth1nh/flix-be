package com.fu.flix.dao.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fu.flix.constant.enums.OTPType;
import com.fu.flix.constant.enums.RedisDataTypeEnum;
import com.fu.flix.dao.RedisDAO;
import com.fu.flix.dto.request.OTPRequest;
import com.fu.flix.entity.OTPInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;

import static com.fu.flix.constant.Constant.REDIS_KEY_OTP_PREFIX;

@Service
public class RedisDAOImpl implements RedisDAO {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final long EXPIRE_TIME_IN_SECONDS = 60;


    @Autowired
    public RedisDAOImpl(RedisTemplate<String, String> redisTemplate,
                        ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public OTPInfo findOTP(OTPRequest OTPRequest, OTPType otpType) {
        String key = REDIS_KEY_OTP_PREFIX + otpType.name() + "_" + OTPRequest.getPhone() + "_" + OTPRequest.getOtp();
        OTPInfo otpInfo = null;
        try {
            Object data = redisTemplate.opsForValue().get(key);
            otpInfo = stringToObject(String.valueOf(data), OTPInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return otpInfo;
    }

    @Override
    public void saveOTP(OTPInfo otpInfo) throws JsonProcessingException {
        String key = REDIS_KEY_OTP_PREFIX + otpInfo.getOtpType().name() + "_" + otpInfo.getUsername() + "_" + otpInfo.getOtp();
        redisTemplate.opsForValue().set(key, objectToString(otpInfo));
        redisTemplate.expireAt(key, Instant.now().plusSeconds(EXPIRE_TIME_IN_SECONDS));
    }

    @Override
    public void deleteOTP(OTPInfo otpInfo) {
        String key = REDIS_KEY_OTP_PREFIX + otpInfo.getOtpType().name() + "_" + otpInfo.getUsername() + "_" + otpInfo.getOtp();
        redisTemplate.delete(key);
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
