package com.fu.flix.service.impl;

import com.ea.async.Async;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fu.flix.configuration.AppConf;
import com.fu.flix.dao.RedisDAO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.entity.OTPInfo;
import com.fu.flix.dto.request.SmsRequest;
import com.fu.flix.service.SmsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static com.fu.flix.constant.Constant.SEND_SMS_OTP_FAILED;

@Service
public class SmsServiceImpl implements SmsService {

    private final AppConf appConf;
    private final RedisDAO redisDAO;

    public SmsServiceImpl(AppConf appConf,
                          RedisDAO redisDAO) {
        this.appConf = appConf;
        this.redisDAO = redisDAO;
    }

    @Override
    public int sendAndSaveOTP(SmsRequest request) throws JsonProcessingException {
        Twilio.init(appConf.getTwilioInfo().getAccountSid(), appConf.getTwilioInfo().getAuthToken());

        int min = 100000;
        int max = 999999;
        int otp = (int) (Math.random() * (max - min + 1) + min);

        Async.await(CompletableFuture.runAsync(() -> sendOTP(request.getPhoneNumberFormatted(), otp)));

        OTPInfo otpInfo = new OTPInfo();
        otpInfo.setOtp(otp);
        otpInfo.setUsername(request.getUsername());
        otpInfo.setOtpType(request.getOtpType());
        redisDAO.saveOTP(otpInfo);

        return otp;
    }

    private void sendOTP(String targetPhone, int otp) {
        String msg = "Mã xác nhận cho ứng dụng FLIX của bạn là " + otp;
        Message.creator(
                        new PhoneNumber(targetPhone),
                        new PhoneNumber(appConf.getTwilioInfo().getFromNumber()), msg)
                .create();
    }
}
