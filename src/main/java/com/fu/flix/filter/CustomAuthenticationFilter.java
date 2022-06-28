package com.fu.flix.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fu.flix.configuration.AppConf;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.response.TokenResponse;
import com.fu.flix.dto.security.UserSecurity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fu.flix.constant.Constant.*;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Slf4j
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final AppConf appConf;
    private final HandlerExceptionResolver resolver;

    @Autowired
    public CustomAuthenticationFilter(AuthenticationManager authenticationManager,
                                      AppConf appConf,
                                      HandlerExceptionResolver resolver) {
        this.authenticationManager = authenticationManager;
        this.appConf = appConf;
        this.resolver = resolver;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        log.info("login with Username is {}", username);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authentication) throws IOException {
        UserSecurity user = (UserSecurity) authentication.getPrincipal();
        Algorithm algorithm = Algorithm.HMAC256(this.appConf.getSecretKey().getBytes());
        String accessToken = JWT.create()
                .withJWTId(String.valueOf(user.getId()))
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + this.appConf.getLifeTimeToke()))
                .withClaim(ROLES, user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .sign(algorithm);

        String refreshToken = JWT.create()
                .withJWTId(String.valueOf(user.getId()))
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + this.appConf.getLifeTimeRefreshToken()))
                .sign(algorithm);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken);
        new ObjectMapper().writeValue(response.getOutputStream(), tokenResponse);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) {
        resolver.resolveException(request, response, null, new GeneralException(FORBIDDEN, LOGIN_FAILED));
    }
}
