package com.fu.flix.configuration;

import com.fu.flix.filter.CustomAuthenticationFilter;
import com.fu.flix.filter.CustomAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.fu.flix.constant.enums.RoleType.*;
import static org.springframework.http.HttpMethod.GET;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final AppConf appConf;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService,
                          BCryptPasswordEncoder bCryptPasswordEncoder,
                          AppConf appConf) {
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.appConf = appConf;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManagerBean(), this.appConf);
        customAuthenticationFilter.setFilterProcessesUrl("/api/v1/login");

        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.authorizeRequests().antMatchers(
                "/api/v1/login/**",
                "/api/v1/token/refresh/**",
                "/api/v1/register/**",
                "/api/v1/address/**",
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/v2/api-docs/**").permitAll();

        http.authorizeRequests().antMatchers(GET, "/api/v1/user/**")
                .hasAnyAuthority(ROLE_CUSTOMER.name(),
                        ROLE_PENDING_REPAIRER.name(),
                        ROLE_REPAIRER.name());

        http.authorizeRequests().antMatchers(GET, "/api/v1/major/**")
                .hasAnyAuthority(ROLE_CUSTOMER.name(),
                        ROLE_PENDING_REPAIRER.name(),
                        ROLE_REPAIRER.name(),
                        ROLE_ADMIN.name());

        http.authorizeRequests().antMatchers("/api/v1/customer/**");

        http.authorizeRequests().anyRequest().authenticated();
        http.addFilter(customAuthenticationFilter);
        http.addFilterBefore(new CustomAuthorizationFilter(this.appConf), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
