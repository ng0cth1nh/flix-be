package com.fu.flix.filter;

import com.fu.flix.dto.error.GeneralException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.fu.flix.constant.Constant.ACCESS_DENIED;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
        throw new GeneralException(HttpStatus.BAD_REQUEST, ACCESS_DENIED);
    }
}
