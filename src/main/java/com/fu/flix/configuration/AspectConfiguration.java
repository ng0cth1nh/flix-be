package com.fu.flix.configuration;

import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.LoginRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Aspect
@Configuration
@Slf4j
public class AspectConfiguration {
    @Before("execution(* com.fu.flix.service.impl.AccountServiceImpl.login(..)) && args(request)")
    public void loginSuccessLogging(LoginRequest request) {
        log.info("User: {} login at: {}", request.getUsername(), LocalDateTime.now());
    }

    @AfterThrowing(pointcut = "execution(* com.fu.flix.service.impl.AccountServiceImpl.login(..)) && args(request)", throwing = "exception")
    public void loginFailLogging(GeneralException exception, LoginRequest request) {
        log.error("User: {} login fail with error message is {} at: {}",
                request.getUsername(),
                exception.getMessage(),
                LocalDateTime.now());
    }
}
