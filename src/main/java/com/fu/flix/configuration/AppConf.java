package com.fu.flix.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Map;

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
    private String firebaseConfig;
    private String defaultIcon;
    private String defaultImage;
    private String[] permitAllApis;
    private Integer defaultLimitQuery;
    private Integer defaultOffset;
    private Double vat;
    private Double profitRate;
    private Long cancelablePendingRequestInterval;
    private Long cancelableFixingRequestInterval;
    private Long cancelableApprovalRequestInterval;
    private Long fine;
    private VnPayInfo vnPayInfo;
    private Notification notification;
    private Long descriptionMaxLength;
    private Long nameMaxLength;
    private Integer defaultPageSize;
    private Integer defaultPageNumber;
    private Long milestoneMoney;
    private Long minVnPay;

    @Data
    public static class Notification {
        private String defaultImageName;
        private String defaultImage;
        private Map<String, String> title;
        private Map<String, String> content;
    }

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
        private String locate;
        private Integer vnPayAmountRate;
        private String datePattern;
        private String currCode;
        private String payUrl;
        private PaymentInfo paymentInfo;
        private DepositInfo depositInfo;
    }

    @Data
    public static class PaymentInfo {
        private String tmnCode;
        private String secureHash;
        private String returnUrl;
    }

    @Data
    public static class DepositInfo {
        private String tmnCode;
        private String secureHash;
        private String returnUrl;
    }
}
