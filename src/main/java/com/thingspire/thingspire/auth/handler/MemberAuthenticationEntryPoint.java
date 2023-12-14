package com.thingspire.thingspire.auth.handler;

import com.thingspire.thingspire.audit.Audit;
import com.thingspire.thingspire.audit.AuditRepository;
import com.thingspire.thingspire.auth.utils.ErrorResponder;
import com.thingspire.thingspire.response.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;

import static com.thingspire.thingspire.auth.utils.ErrorResponder.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberAuthenticationEntryPoint implements AuthenticationEntryPoint {
    final AuditRepository auditRepository;
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        Exception exception = (Exception) request.getAttribute("exception");

        //ErrorResponder.sendErrorResponse(response, HttpStatus.UNAUTHORIZED);
        logExceptionMessage(authException, exception);
        ErrorResponder.sendErrorResponse(response, authException, exception);

//        Audit audit = new Audit();
//
//        audit.setActivityTime(LocalDateTime.now());
//        audit.setName("user");
//        audit.setDetail(authException.getMessage());
//        audit.setSuccess("실패");
//        audit.setActivityType("자격증명");
//        audit.setLoginId("user");
//        auditRepository.save(audit);

    }
    public static void logExceptionMessage(AuthenticationException authException, Exception exception) {
        String message = exception != null ? exception.getMessage() : authException.getMessage();

        log.warn("Unauthorized error happened: {}", message);
    }
}