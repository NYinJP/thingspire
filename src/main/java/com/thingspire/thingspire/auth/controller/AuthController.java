package com.thingspire.thingspire.auth.controller;

import com.thingspire.thingspire.auth.jwt.JwtTokenizer;
import com.thingspire.thingspire.auth.service.MemberDetailService;
import com.thingspire.thingspire.auth.service.SecurityUtils;
import com.thingspire.thingspire.exception.BusinessLogicException;
import com.thingspire.thingspire.exception.ExceptionCode;
import com.thingspire.thingspire.user.Member;
import com.thingspire.thingspire.user.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import static com.thingspire.thingspire.auth.filter.JwtAuthenticationFilter.MEMBERS_REFRESH_TOKEN_CACHE;

/*
    accesToken 재발급 로직 수행하는 컨트롤러
*/
@RestController
@RequestMapping("/api")
public class AuthController {

    private final JwtTokenizer jwtTokenizer;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final MemberRepository memberRepository;
    private final CacheManager cacheManager;
    private final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(JwtTokenizer jwtTokenizer, AuthenticationManagerBuilder authenticationManagerBuilder, MemberRepository memberRepository, CacheManager cacheManager) {
        this.jwtTokenizer = jwtTokenizer;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.memberRepository = memberRepository;
        this.cacheManager = cacheManager;
    }

    @PostMapping("/authentication/token")
    public ResponseEntity<JWTToken> authorize(@RequestHeader("Authorization") String refresh) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info(authentication.toString());

        // 현재 인증된 사용자의 memberId 가져오기

        Long memberId = memberRepository.findMemberIdByLoginId(authentication.getPrincipal().toString()).get().getMemberId();

        String refreshToken = refresh;






        String cacheKey = memberId.toString();
        String storeMemberToken = cacheManager.getCache(MEMBERS_REFRESH_TOKEN_CACHE).get(cacheKey, String.class);
        if (org.apache.commons.lang3.StringUtils.isBlank(storeMemberToken)) {
            throw new BusinessLogicException(ExceptionCode.JWT_TOKEN_UNAUTHORIZED);
        }
        log.info("##### refreshToken : {}", refreshToken);
        log.info("##### storeMemberToken : {}", storeMemberToken);

        if (!refreshToken.equals(storeMemberToken)) {
            throw new BusinessLogicException(ExceptionCode.REFRESHTOKEN_NOT_SAME);
        }

        HashMap<String, Object> extraClaims = new HashMap<>();

        Optional<Member> optionalMember = memberRepository.findById(memberId);
        optionalMember.ifPresent(member -> {
            extraClaims.put("memberId", optionalMember.get().getMemberId());
            extraClaims.put("loginId", optionalMember.get().getLoginId());
            extraClaims.put("ID", optionalMember.get().getLoginId());
            extraClaims.put("departmentType", optionalMember.get().getDepartmentType());
            extraClaims.put("name", optionalMember.get().getName());
            extraClaims.put("roles", optionalMember.get().getRoles());
            extraClaims.put("authority", optionalMember.get().getAuthority());
            extraClaims.put("email", optionalMember.get().getEmail());

            // 등등...
        });


        String subject = memberRepository.findById(memberId).get().getEmail();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

        // 만약 Cache에 저장되어있던 refreshToken이 만료됐다면

        if (!isRefreshTokenValid(resolveToken(storeMemberToken))) { // 기한이 남아있다면
            // String newRefreshToken = jwtTokenizer.generateRefreshToken(extraClaims, subject, expiration, base64EncodedSecretKey);
            // cacheManager.getCache(MEMBERS_REFRESH_TOKEN_CACHE).put(cacheKey, "Bearer " + storeMemberToken);
            String accessToken = jwtTokenizer.generateAccessToken(extraClaims, subject, expiration, base64EncodedSecretKey);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);
            return new ResponseEntity<>(new JWTToken(accessToken, resolveToken(storeMemberToken)), headers, HttpStatus.OK);

        } else { // 기한이 만료됐다면 -> ERROR! 다시 로그인해서 재발급 받아!
            throw new BusinessLogicException(ExceptionCode.REFRESHTOKEN_NOT_VALID);
        }

        // 만약 refreshToken 만료 시간 이전이라면
        // Cache - RefreshToken 저장
        // accessToken 새롭게 생성

    }

    private String resolveToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    boolean isRefreshTokenValid(String refreshToken) { // 만료가 맞다면 true, 기한이 남아있다면 false
        Long refreshTokenExpiredTime = jwtTokenizer.getExpiration(refreshToken);
        log.info("refreshToken 의 만료시간:  " + refreshTokenExpiredTime);
        Long currentTime = System.currentTimeMillis();

        return refreshTokenExpiredTime < currentTime; // 만료가 맞다면 true, 기한이 남아있다면 false
    }
    static class JWTToken {

        private String accessToken;

        private String refreshToken;

        JWTToken(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}
