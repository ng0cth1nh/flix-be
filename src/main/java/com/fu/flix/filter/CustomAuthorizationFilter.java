package com.fu.flix.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fu.flix.configuration.AppConf;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.security.UserPrincipal;
import com.fu.flix.entity.Role;
import com.fu.flix.service.ValidatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.fu.flix.constant.Constant.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Slf4j
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    private final AppConf appConf;
    private final HandlerExceptionResolver resolver;

    private final ValidatorService validatorService;

    public CustomAuthorizationFilter(AppConf appConf,
                                     HandlerExceptionResolver resolver,
                                     ValidatorService validatorService) {
        this.appConf = appConf;
        this.resolver = resolver;
        this.validatorService = validatorService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (isPermitAll(request.getServletPath())) {
            filterChain.doFilter(request, response);
        } else {
            String authorizationHeader = request.getHeader(AUTHORIZATION);
            if (authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
                try {
                    String token = authorizationHeader.substring(BEARER.length());
                    Algorithm algorithm = Algorithm.HMAC256(this.appConf.getSecretKey().getBytes());
                    JWTVerifier verifier = JWT.require(algorithm).build();
                    DecodedJWT decodedJWT = verifier.verify(token);
                    String username = decodedJWT.getSubject();
                    Long id = decodedJWT.getClaim(USER_ID).asLong();

                    List<String> roles = validatorService.getUserValidated(id)
                            .getRoles()
                            .stream()
                            .map(Role::getName)
                            .collect(Collectors.toList());

                    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    for (String role : roles) {
                        authorities.add(new SimpleGrantedAuthority(role));
                    }
                    UsernamePasswordAuthenticationToken authenticationToken
                            = new UsernamePasswordAuthenticationToken(new UserPrincipal(id, username, roles), null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    filterChain.doFilter(request, response);
                } catch (Exception exception) {
                    resolver.resolveException(request, response, null, exception);
                }
            } else {
                resolver.resolveException(request, response, null, new GeneralException(FORBIDDEN, ACCESS_DENIED));
            }
        }

    }

    private boolean isPermitAll(String api) {
        String[] permitAllApis = appConf.getPermitAllApis();
        return Arrays.asList(permitAllApis).contains(api);
    }
}
