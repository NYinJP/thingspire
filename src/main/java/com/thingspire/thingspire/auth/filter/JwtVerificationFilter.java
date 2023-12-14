package com.thingspire.thingspire.auth.filter;

import com.thingspire.thingspire.audit.Audit;
import com.thingspire.thingspire.audit.AuditRepository;
import com.thingspire.thingspire.auth.jwt.JwtTokenizer;
import com.thingspire.thingspire.auth.utils.CustomAuthorityUtils;
import com.thingspire.thingspire.auth.utils.ErrorResponder;
import com.thingspire.thingspire.exception.BusinessLogicException;
import com.thingspire.thingspire.exception.ExceptionCode;
import com.thingspire.thingspire.response.ErrorResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

import static com.thingspire.thingspire.auth.utils.ErrorResponder.sendErrorResponse;


/*
클라이언트 측에서 전송된 request header에 포함된 JWT에 대해 검증 작업을 수행하는 클래스
 */
@Slf4j
public class JwtVerificationFilter extends OncePerRequestFilter {  // (1)
    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;

    public JwtVerificationFilter(JwtTokenizer jwtTokenizer, CustomAuthorityUtils authorityUtils) {
        this.jwtTokenizer = jwtTokenizer;
        this.authorityUtils = authorityUtils;
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
            String requestUri = request.getRequestURI();
            log.info("#### 현재 요청 URL :" + requestUri);

            Map<String, Object> claims = verifyJws(request);
            setAuthenticationToContext(claims);
            filterChain.doFilter(request, response);

        } catch (SignatureException se) { // 서명 검증 실패

            String requestUri = request.getRequestURI();

            log.info("Invalid JWT signature.");
            request.setAttribute("exception", se);

            if (requestUri.equals("/api/authentication/token")) { // refreshToken 검증 오류
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid JWT signature");
            } else { // accessToken 검증 오류
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid JWT signature");
            }


        } catch (ExpiredJwtException ee) { // JWT 토큰 만료!
            String requestUri = request.getRequestURI();

            log.info("Expired JWT token.");
            request.setAttribute("exception", ee);

            if (requestUri.equals("/api/authentication/token")) { // refreshToken 검증 오류
                sendErrorResponse(response, HttpStatus.PAYMENT_REQUIRED, "Request failed with status code 402");
            } else { // accessToken 검증 오류
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Request failed with status code 401");
            }

        } catch (Exception e) {
            String requestUri = request.getRequestURI();
            request.setAttribute("exception", e);

            if (requestUri.equals("/api/authentication/token")) { // refreshToken 검증 오류
                sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
            } else { // accessToken 검증 오류
                sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
            }

        }
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
