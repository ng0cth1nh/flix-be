package com.fu.flix.configuration;

import com.fu.flix.filter.CustomAccessDeniedHandler;
import com.fu.flix.filter.CustomAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static com.fu.flix.constant.enums.RoleType.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AppConf appConf;

    private final HandlerExceptionResolver resolver;

    @Autowired
    public SecurityConfig(AppConf appConf,
                          @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.appConf = appConf;
        this.resolver = resolver;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable();
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
        http.addFilterBefore(new CustomAuthorizationFilter(this.appConf, resolver), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication();
    }
}
