package com.fpt.flix.flix_app.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.flix.flix_app.configurations.AppConf;
import com.fpt.flix.flix_app.models.db.OTPInfo;
import com.fpt.flix.flix_app.models.requests.SmsRequest;
import com.fpt.flix.flix_app.repositories.RedisRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
public class SmsService {

    private final AppConf appConf;
    private final RedisRepository redisRepository;

    public SmsService(AppConf appConf,
                      RedisRepository redisRepository) {
        this.appConf = appConf;
        this.redisRepository = redisRepository;
    }

    public int sendAndSaveOTP(SmsRequest request) throws JsonProcessingException {
        Twilio.init(appConf.getTwilioInfo().getAccountSid(), appConf.getTwilioInfo().getAuthToken());

        int min = 100000;
        int max = 999999;
        int otp = (int) (Math.random() * (max - min + 1) + min);

        String msg = "Your OTP is " + otp + ". Please verify this OTP";
        Message
                .creator(new PhoneNumber(request.getPhoneNumber()), new PhoneNumber(appConf.getTwilioInfo().getFromNumber()), msg)
                .create();

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setOtp(otp);
        otpInfo.setUsername(request.getUsername());
        redisRepository.saveOTP(otpInfo);

        return otp;
    }
}
