package com.thingspire.thingspire.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thingspire.thingspire.user.Member;
import com.thingspire.thingspire.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class MemberAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 인증 성공시 memberId 값 반환
        Map<String, Long> data = new HashMap<>();
        Member findMember = memberRepository.findByEmail(authentication.getName()).orElseThrow();

        data.put("memberId", findMember.getMemberId());

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(data);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);

        log.info("# Authentication Success");
    }
}
