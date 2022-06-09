package com.fu.flix.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
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

    // config for swagger
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }
}
