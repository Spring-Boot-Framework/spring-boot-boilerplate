package com.springboilerplate.springboilerplate.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@EnableWebSecurity
@Configuration
@Order(1)
public class StatelessAuthenticationSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private StatelessAuthenticationFilter statelessAuthenticationFilter;
    @Autowired
    private TokenAuthenticationService tokenAuthenticationService;
    @Autowired
    private CustomUserService userService;

    @Autowired
    private Environment env;

    public StatelessAuthenticationSecurityConfig() {
        super(true);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.cors().and().exceptionHandling().and().anonymous()
                .and().servletApi().and().headers().cacheControl();

        http.authorizeRequests()
                .antMatchers("/v1/users/register").permitAll()
                .antMatchers("/v1/**").authenticated()
                .antMatchers("/v1/users/*").authenticated()
                .antMatchers("/v1/passwordResetToken/resetPassword").authenticated()
                .and()
                .addFilterBefore(new JwtLoginFilter("/login", authenticationManager(),
                                tokenAuthenticationService, userService),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(statelessAuthenticationFilter, AbstractPreAuthenticatedProcessingFilter.class);

    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("PUT", "DELETE", "POST", "GET", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Origin", "X-Requested-With", "Content-Type", "Accept", "Authorization"));
        configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials", "authorization"));
        configuration.setMaxAge(3600L);
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        String passwordKey = env.getProperty("MOBSTAFF_PASSWORD_KEY");
        auth.userDetailsService(userService)
                    .passwordEncoder(new StandardPasswordEncoder(passwordKey));
    }
 }

