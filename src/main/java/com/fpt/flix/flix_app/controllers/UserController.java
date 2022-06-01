package com.fpt.flix.flix_app.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.flix.flix_app.configurations.AppConf;
import com.fpt.flix.flix_app.models.db.Role;
import com.fpt.flix.flix_app.models.db.User;
import com.fpt.flix.flix_app.models.errors.GeneralException;
import com.fpt.flix.flix_app.models.responses.TokenResponse;
import com.fpt.flix.flix_app.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fpt.flix.flix_app.constants.Constant.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@Slf4j
@RequestMapping("api/v1/")
public class UserController {
    private final UserService userService;

    private final AppConf appConf;

    public UserController(UserService userService,
                          AppConf appConf) {
        this.userService = userService;
        this.appConf = appConf;
    }

    @GetMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request,
                             HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
            try {
                String refreshToken = authorizationHeader.substring(BEARER.length());
                Algorithm algorithm = Algorithm.HMAC256(this.appConf.getSecretKey().getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(refreshToken);
                String username = decodedJWT.getSubject();
                User user = userService.getUser(username);

                String accessToken = JWT.create()
                        .withSubject(user.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis() + this.appConf.getLifeTimeToke()))
                        .withIssuer(request.getRequestURI())
                        .withClaim(ROLES, user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                        .sign(algorithm);

                TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokenResponse);
            } catch (Exception exception) {

                response.setStatus(FORBIDDEN.value());
                Map<String, String> errors = new HashMap<>();
                errors.put("message", REFRESH_TOKEN_INVALID);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), errors);
            }
        } else {
            throw new GeneralException(REFRESH_TOKEN_MISSING);
        }
    }

}
