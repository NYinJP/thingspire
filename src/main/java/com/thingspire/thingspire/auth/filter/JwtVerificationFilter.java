package com.thingspire.thingspire.auth.filter;

import com.thingspire.thingspire.audit.Audit;
import com.thingspire.thingspire.audit.AuditRepository;
import com.thingspire.thingspire.auth.jwt.JwtTokenizer;
import com.thingspire.thingspire.auth.utils.CustomAuthorityUtils;
import com.thingspire.thingspire.exception.BusinessLogicException;
import com.thingspire.thingspire.exception.ExceptionCode;
import com.thingspire.thingspire.redis.RedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import io.jsonwebtoken.security.SignatureException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.thingspire.thingspire.auth.handler.MemberLogoutHandler.extractTokenFromRequest;

/*
클라이언트 측에서 전송된 request header에 포함된 JWT에 대해 검증 작업을 수행하는 클래스
 */
@Slf4j
public class JwtVerificationFilter extends OncePerRequestFilter {  // (1)
    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;
    private final RedisUtil redisUtil;

    public JwtVerificationFilter(JwtTokenizer jwtTokenizer, CustomAuthorityUtils authorityUtils, RedisUtil redisUtil) {
        this.jwtTokenizer = jwtTokenizer;
        this.authorityUtils = authorityUtils;
        this.redisUtil = redisUtil;
    }

    // (2)
//    public JwtVerificationFilter(JwtTokenizer jwtTokenizer,
//                                 CustomAuthorityUtils authorityUtils) {
//        this.jwtTokenizer = jwtTokenizer;
//        this.authorityUtils = authorityUtils;
//    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, ServletException, IOException {
        try {
            String token = extractTokenFromRequest(request);

            // 블랙리스트 확인
            if (redisUtil.hasKeyBlackList(token)) {
                // 블랙리스트에 등재된 토큰인 경우, 예외를 발생시켜 검증 실패 처리
                throw new BadCredentialsException("The token is blacklisted");
            }
            Map<String, Object> claims = verifyJws(request);
            setAuthenticationToContext(claims);
        }catch(SignatureException se) { // 서명 검증 실패
            log.info("Invalid JWT signature.");
            request.setAttribute("exception", se);
        }catch (ExpiredJwtException ee) {
            log.info("Expired JWT token.");
            request.setAttribute("exception", ee);
        } catch (Exception e) {
            request.setAttribute("exception", e);
        }

        filterChain.doFilter(request, response);
    }

    // (6)
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String authorization = request.getHeader("Authorization");  // (6-1)

        return authorization == null || !authorization.startsWith("Bearer");  // (6-2)
    }


    private Map<String, Object> verifyJws(HttpServletRequest request) {
        String jws = request.getHeader("Authorization").replace("Bearer ", ""); // (3-1)
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey()); // (3-2)
        Map<String, Object> claims = jwtTokenizer.getClaims(jws, base64EncodedSecretKey).getBody();   // (3-3)

        return claims;
    }

    // Authentication 객체를 SecurityContext에 저장하기 위한 메서드
    private void setAuthenticationToContext(Map<String, Object> claims) {
        // String username = (String) claims.get("name");   // (4-1)
        // Long memberId = (Long) claims.get("memberId");
        String loginId = (String) claims.get("loginId");
        List<GrantedAuthority> authorities = authorityUtils.createAuthorities((List)claims.get("roles"));  // (4-2)
        Authentication authentication = new UsernamePasswordAuthenticationToken(loginId, null, authorities);  // (4-3)
        SecurityContextHolder.getContext().setAuthentication(authentication); // (4-4)
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        // Token 추출 로직을 적절히 구현
        // 예: Authorization 헤더에서 Bearer 토큰 추출
        String authorizationHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
