package com.thingspire.thingspire.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thingspire.thingspire.audit.AuditRepository;
import com.thingspire.thingspire.auth.dto.LoginDTO;
import com.thingspire.thingspire.auth.filter.JwtAuthenticationFilter;
import com.thingspire.thingspire.auth.filter.JwtVerificationFilter;
import com.thingspire.thingspire.auth.handler.*;
import com.thingspire.thingspire.auth.jwt.JwtTokenizer;
import com.thingspire.thingspire.auth.utils.CustomAuthorityUtils;
import com.thingspire.thingspire.user.repository.MemberRepository;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration {
    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;
    private final CustomAuthorityUtils customAuthorityUtils;
    private final MemberRepository memberRepository;
    private final AuditRepository auditRepository;
    private final LoginDTO loginDTO;
    private final CacheManager cacheManager;

//    public SecurityConfiguration(JwtTokenizer jwtTokenizer, CustomAuthorityUtils authorityUtils, CustomAuthorityUtils customAuthorityUtils, MemberRepository memberRepository, AuditRepository auditRepository, LoginDTO loginDTO) {
//        this.jwtTokenizer = jwtTokenizer;
//        this.authorityUtils = authorityUtils;
//        this.customAuthorityUtils = customAuthorityUtils;
//        this.memberRepository = memberRepository;
//        this.auditRepository = auditRepository;
//        this.loginDTO = loginDTO;
//    }

//    public SecurityConfiguration(JwtTokenizer jwtTokenizer, CustomAuthorityUtils authorityUtils, CustomAuthorityUtils customAuthorityUtils, MemberRepository memberRepository, AuditRepository auditRepository, LoginDTO loginDTO ) {
//        this.jwtTokenizer = jwtTokenizer;
//        this.authorityUtils = authorityUtils;
//        this.customAuthorityUtils = customAuthorityUtils;
//        this.memberRepository = memberRepository;
//        this.auditRepository = auditRepository;
//        this.loginDTO = loginDTO;
//    }


    public SecurityConfiguration(JwtTokenizer jwtTokenizer, CustomAuthorityUtils authorityUtils, CustomAuthorityUtils customAuthorityUtils, MemberRepository memberRepository, AuditRepository auditRepository, LoginDTO loginDTO, CacheManager cacheManager) {
        this.jwtTokenizer = jwtTokenizer;
        this.authorityUtils = authorityUtils;
        this.customAuthorityUtils = customAuthorityUtils;
        this.memberRepository = memberRepository;
        this.auditRepository = auditRepository;
        this.loginDTO = loginDTO;
        this.cacheManager = cacheManager;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .headers().frameOptions().sameOrigin()
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource())
                .and()
                //.requiresChannel().anyRequest().requiresSecure()
                //.and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .formLogin().disable()
                .httpBasic().disable()
                .exceptionHandling()
                .authenticationEntryPoint(new MemberAuthenticationEntryPoint(auditRepository))
                .accessDeniedHandler(new MemberAccessDeniedHandler())
                .and()
                .apply(new CustomFilterConfigurer())


                .and()
                .authorizeHttpRequests(authorize -> authorize
//                        .antMatchers(HttpMethod.GET, "/*/users/**").hasAnyRole("USER","ADMIN")
//                        .antMatchers(HttpMethod.GET,"/*/users").hasRole("ADMIN")
//                        .antMatchers(HttpMethod.POST,"/*/users").hasRole("ADMIN")
//                        .antMatchers(HttpMethod.DELETE,"/*/users/**").hasAnyRole("USER", "ADMIN")
//
//                        .antMatchers(HttpMethod.GET,"/*/api/audits").hasRole("ADMIN")
                        .anyRequest().permitAll()
                );
        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() { //memberService에서 DI 받아 사용
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("*"));
        configuration.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    public class CustomFilterConfigurer extends AbstractHttpConfigurer<CustomFilterConfigurer, HttpSecurity> {
        @Override
        public void configure(HttpSecurity builder) throws Exception {

            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);

            JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtTokenizer, cacheManager);  // (2-4)
            jwtAuthenticationFilter.setFilterProcessesUrl("/api/login");
            jwtAuthenticationFilter.setAuthenticationSuccessHandler(new MemberAuthenticationSuccessHandler(auditRepository));
            jwtAuthenticationFilter.setAuthenticationFailureHandler(new MemberAuthenticationFailureHandler(auditRepository));

            JwtVerificationFilter jwtVerificationFilter = new JwtVerificationFilter(jwtTokenizer, authorityUtils);

            builder.addFilter(jwtAuthenticationFilter)
                    .addFilterAfter(jwtVerificationFilter, JwtAuthenticationFilter.class);
        }
    }
}
