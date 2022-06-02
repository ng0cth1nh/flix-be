package com.fpt.flix.flix_app.configurations;

import com.fpt.flix.flix_app.filters.CustomAuthenticationFilter;
import com.fpt.flix.flix_app.filters.CustomAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.fpt.flix.flix_app.constants.RoleType.ROLE_ADMIN;
import static com.fpt.flix.flix_app.constants.RoleType.ROLE_CUSTOMER;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

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
        http.authorizeRequests().antMatchers("/api/v1/login/**",
                "/api/v1/token/refresh/**",
                "/api/v1/register/**").permitAll();
//        http.authorizeRequests().antMatchers(GET, "/api/v1/user/**").hasAnyAuthority(ROLE_CUSTOMER.name());
//        http.authorizeRequests().antMatchers(POST, "/api/v1/user/save/**").hasAnyAuthority(ROLE_ADMIN.name());
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
