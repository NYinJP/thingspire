package com.thingspire.thingspire.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thingspire.thingspire.audit.Audit;
import com.thingspire.thingspire.audit.AuditRepository;
import com.thingspire.thingspire.auth.dto.LoginDTO;
import com.thingspire.thingspire.auth.service.MemberDetailService;
import com.thingspire.thingspire.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;

@Slf4j
@Component
public class MemberAuthenticationFailureHandler implements AuthenticationFailureHandler {  // (1)

    private AuditRepository auditRepository;

//    @Autowired
//    public MemberAuthenticationFailureHandler(AuditRepository auditRepository) {
//        this.auditRepository = auditRepository;
//    }

    @Autowired
    public MemberAuthenticationFailureHandler(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {


        String errorMessage;
        if (exception instanceof BadCredentialsException || exception instanceof InternalAuthenticationServiceException) {
            errorMessage = "아이디 또는 비밀번호가 맞지 않습니다.";
        } else if (exception instanceof UsernameNotFoundException) {
            errorMessage = "계정이 존재하지 않습니다.";
        } else if (exception instanceof AuthenticationCredentialsNotFoundException) {
            errorMessage = "인증 요청이 거부되었습니다. 관리자에게 문의하세요.";
        } else {
            errorMessage = "알 수 없는 이유로 로그인에 실패하였습니다. 관리자에게 문의하세요.";
        }


        // 인증 실패 시, 에러 로그를 기록하거나 error response를 전송할 수 있다.
        log.error("# Authentication failed: {}", exception.getMessage());

//        ObjectMapper obj = new ObjectMapper();
//        loginDTO = obj.readValue(request.getInputStream(), LoginDTO.class);

        String name = request.getParameter("username");
        System.out.println(name);

        Audit audit = new Audit();

        if (exception instanceof BadCredentialsException) {
            BadCredentialsException badCredentialsException = (BadCredentialsException) exception;
            String message = badCredentialsException.getMessage();

            String username = request.getParameter("username"); //null값임
            log.info("@@@@@@@@@@@ : " + username);
//            String username = (String) authentication.getPrincipal();
//            String password = (String) authentication.getCredentials();
            audit.setLoginId(message);
            audit.setName(message);
        }

        audit.setActivityType("로그인");
        audit.setDetail("사용자 로그인");
        audit.setActivityTime(LocalDateTime.now());

        audit.setSuccess("실패");
        auditRepository.save(audit);

        sendErrorResponse(response, errorMessage);  // (2)

    }

    private void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
        Gson gson = new Gson();     // (2-1)

        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.UNAUTHORIZED, errorMessage); // (2-2)

//response.setContentType(MediaType.APPLICATION_JSON_VALUE);    // (2-3)
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());          // (2-4)

        response.getWriter().write(gson.toJson(errorResponse, ErrorResponse.class));   // (2-5)
    }
}
