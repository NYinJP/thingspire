package com.thingspire.thingspire.auth.handler;


import com.thingspire.thingspire.audit.Audit;
import com.thingspire.thingspire.audit.AuditRepository;
import com.thingspire.thingspire.auth.service.MemberDetailService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
public class MemberAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private AuditRepository auditRepository;

    @Autowired
    public MemberAuthenticationSuccessHandler(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // 인증 성공 후, 로그를 기록하거나 사용자 정보를 response로 전송하는 등의 추가 작업을 할 수 있다.
        log.info("# Authenticated successfully!");

        Audit audit = new Audit();
        audit.setActivityType("로그인");
        audit.setDetail("사용자 로그인");
        audit.setActivityTime(LocalDateTime.now());

        Object principal = authentication.getPrincipal();

        if(principal instanceof MemberDetailService.MemberDetails){
            MemberDetailService.MemberDetails memberDetails = (MemberDetailService.MemberDetails) principal;

            audit.setName(memberDetails.getName());
            audit.setLoginId(memberDetails.getLoginId());
        }
        audit.setSuccess("성공");

        auditRepository.save(audit);
    }
}