package com.thingspire.thingspire.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thingspire.thingspire.auth.jwt.JwtTokenizer;
import com.thingspire.thingspire.auth.dto.LoginDTO;
import com.thingspire.thingspire.user.Member;
import lombok.SneakyThrows;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;


public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {  // (1)
    private final AuthenticationManager authenticationManager;
    private final JwtTokenizer jwtTokenizer;


    // (2)
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtTokenizer jwtTokenizer) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenizer = jwtTokenizer;
    }

    // (3)
    @SneakyThrows
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {

        ObjectMapper objectMapper = new ObjectMapper();    // (3-1)
        LoginDTO loginDto = objectMapper.readValue(request.getInputStream(), LoginDTO.class); // (3-2)

        // (3-3)
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getLoginId(), loginDto.getPassword());

        return authenticationManager.authenticate(authenticationToken);  // (3-4)
    }

    // (4)
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws ServletException, IOException {
        Member member = (Member) authResult.getPrincipal();  // (4-1)

        String accessToken = delegateAccessToken(member);   // (4-2)
        String refreshToken = delegateRefreshToken(member); // (4-3)

        response.setHeader("Authorization", "Bearer " + accessToken);  // (4-4)
        response.setHeader("memberId", String.valueOf(member.getMemberId()));
        response.setHeader("role", String.valueOf(member.getRoles()));
        response.setHeader("Refresh", refreshToken);                   // (4-5)
        response.setHeader("departmentType",member.getDepartmentType()); // 부서타입

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("status", "success");
        responseMap.put("message", "로그인이 성공했습니다.");

//        Map<String, Object> responseMap = new HashMap<>();

        if(member.getAuthority().equals("Admin")) {
            responseMap.put("department", null);
            responseMap.put("departmentType", null);
        }else{
            responseMap.put("department", member.getDepartment());
            responseMap.put("departmentType", member.getDepartmentType());
        }
        responseMap.put("accessToken", accessToken);
        responseMap.put("refreshToken", refreshToken);
        responseMap.put("memberId", String.valueOf(member.getMemberId()));
        responseMap.put("password", member.getPassword());
        responseMap.put("authority", member.getAuthority());
        responseMap.put("factoryCode", member.getFactoryCode());
        responseMap.put("email", member.getEmail());
        responseMap.put("name", member.getName());
        responseMap.put("phoneNumber", member.getPhoneNumber());
        responseMap.put("loginId", member.getLoginId());

        ObjectMapper objectMapper = new ObjectMapper();
        String responseBody = objectMapper.writeValueAsString(responseMap);
        response.getWriter().write(responseBody);

        this.getSuccessHandler().onAuthenticationSuccess(request, response, authResult);
    }

    // (5)
    private String delegateAccessToken(Member member) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("ID", member.getLoginId());
        claims.put("roles", member.getRoles());
        claims.put("memberId", member.getMemberId());
        claims.put("name", member.getName());
        claims.put("loginId", member.getLoginId());
        claims.put("departmentType", member.getDepartmentType());
        claims.put("authority", member.getAuthority());
        claims.put("factoryCode", member.getFactoryCode());
        claims.put("phoneNumber", member.getPhoneNumber());
        claims.put("email", member.getEmail());


        String subject = member.getEmail();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());

        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

        String accessToken = jwtTokenizer.generateAccessToken(claims, subject, expiration, base64EncodedSecretKey);

        return accessToken;
    }

    // (6)
    private String delegateRefreshToken(Member member) {
        String subject = member.getEmail();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getRefreshTokenExpirationMinutes());
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

        String refreshToken = jwtTokenizer.generateRefreshToken(subject, expiration, base64EncodedSecretKey);

        return refreshToken;
    }
}
