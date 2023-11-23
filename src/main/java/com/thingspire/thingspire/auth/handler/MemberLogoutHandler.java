package com.thingspire.thingspire.auth.handler;

import com.thingspire.thingspire.auth.jwt.JwtTokenizer;
import com.thingspire.thingspire.auth.service.MemberDetailService;
import com.thingspire.thingspire.redis.RedisUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MemberLogoutHandler implements LogoutHandler {
    private final JwtTokenizer jwtTokenizer;
    private final RedisUtil redisUtil;

    public MemberLogoutHandler(JwtTokenizer jwtTokenizer, RedisUtil redisUtil) {
        this.jwtTokenizer = jwtTokenizer;
        this.redisUtil = redisUtil;
    }
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // AccessToken 가져오기
        String accessToken = extractTokenFromRequest(request, "Authorization");
        // RefreshToken 가져오기
        String refreshToken = extractTokenFromRequest(request, "Refresh");

        // AccessToken blackList에 등록하여 만료시키기
        // 해당 액세스 토큰의 남은 유효시간을 얻음
        Long expiration = jwtTokenizer.getExpiration(accessToken);
        redisUtil.setBlackList(accessToken, "access_token", expiration);
    }

    public static String extractTokenFromRequest(HttpServletRequest request, String headerName) {
        String headerValue = request.getHeader(headerName);

        if (headerValue != null && headerValue.startsWith("Bearer")) {
            return headerValue.substring(7);
        } else if (headerValue.startsWith("ey")) {
            return headerValue;
        }
        return null;
    }
}
