package com.fu.flix.configuration;

import com.fu.flix.filter.CustomAccessDeniedHandler;
import com.fu.flix.filter.CustomAuthenticationFilter;
import com.fu.flix.filter.CustomAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.web.servlet.HandlerExceptionResolver;

import static com.fu.flix.constant.enums.RoleType.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final AppConf appConf;

    private final HandlerExceptionResolver resolver;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService,
                          BCryptPasswordEncoder bCryptPasswordEncoder,
                          AppConf appConf,
                          @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.appConf = appConf;
        this.resolver = resolver;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CustomAuthenticationFilter customAuthenticationFilter
                = new CustomAuthenticationFilter(authenticationManagerBean(), this.appConf, resolver);
        customAuthenticationFilter.setFilterProcessesUrl("/api/v1/login");

        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.authorizeRequests().antMatchers(appConf.getPermitAllApis())
                .permitAll();

        http.authorizeRequests().antMatchers("/api/v1/repairer/**")
                .hasAnyAuthority(ROLE_REPAIRER.name());

        http.authorizeRequests().antMatchers("/api/v1/commonRepairer/**")
                .hasAnyAuthority(ROLE_REPAIRER.name(),
                        ROLE_PENDING_REPAIRER.name());

        http.authorizeRequests().antMatchers("/api/v1/confirmedUser/**")
                .hasAnyAuthority(ROLE_CUSTOMER.name(),
                        ROLE_REPAIRER.name());

        http.authorizeRequests().antMatchers("/api/v1/user/**",
                        "/api/v1/forgot/password/reset")
                .hasAnyAuthority(ROLE_CUSTOMER.name(),
                        ROLE_PENDING_REPAIRER.name(),
                        ROLE_REPAIRER.name(),
                        ROLE_STAFF.name(),
                        ROLE_MANAGER.name());

        http.authorizeRequests().antMatchers("/api/v1/admin/**")
                .hasAnyAuthority(ROLE_STAFF.name(),
                        ROLE_MANAGER.name());

        http.authorizeRequests().antMatchers("/api/v1/category/**")
                .hasAnyAuthority(ROLE_CUSTOMER.name(),
                        ROLE_PENDING_REPAIRER.name(),
                        ROLE_REPAIRER.name());

        http.authorizeRequests().antMatchers("/api/v1/customer/**")
                .hasAnyAuthority(ROLE_CUSTOMER.name());

        http.authorizeRequests().anyRequest().authenticated();
        http.exceptionHandling().accessDeniedHandler(new CustomAccessDeniedHandler());
        http.addFilter(customAuthenticationFilter);
        http.addFilterBefore(new CustomAuthorizationFilter(this.appConf, resolver), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
