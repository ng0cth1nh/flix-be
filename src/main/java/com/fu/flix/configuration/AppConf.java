package com.fu.flix.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@ConfigurationProperties(prefix = "app")
@Data
@Component
public class AppConf {
    private String secretKey;
    private Long lifeTimeToke;
    private Long lifeTimeRefreshToken;
    private TwilioInfo twilioInfo;
    private Long defaultAvatar;
    private String[] permitAllApis;
    private Integer defaultLimitQuery;
    private Integer defaultOffset;
    private Double vat;
    private Double profitRate;
    private Long minTimeFined;
    private Long fine;
    private VnPayInfo vnPayInfo;
    private Long descriptionMaxLength;
    private Long nameMaxLength;
    private Integer defaultPageSize;
    private Integer defaultPageNumber;

    @Data
    public static class TwilioInfo {
        private String accountSid;
        private String authToken;
        private String fromNumber;
    }

    @Data
    public static class VnPayInfo {
        private String version;
        private String command;
        private String tmnCode;
        private String locate;
        private Integer vnPayAmountRate;
        private String datePattern;
        private String currCode;
        private String secureHash;
        private String payUrl;
        private String returnUrl;
    }

}
