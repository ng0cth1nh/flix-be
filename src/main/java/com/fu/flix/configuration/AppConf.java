package com.fu.flix.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppConf {
    private String secretKey;
    private Long lifeTimeToke;
    private Long lifeTimeRefreshToken;
    private TwilioInfo twilioInfo;

    @Data
    public static class TwilioInfo {
        private String accountSid;
        private String authToken;
        private String fromNumber;
    }
}
