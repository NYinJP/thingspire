package com.thingspire.thingspire.audit;

import com.thingspire.thingspire.exception.BusinessLogicException;
import com.thingspire.thingspire.exception.ExceptionCode;
import com.thingspire.thingspire.user.Member;
import com.thingspire.thingspire.user.MemberService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {
    private final AuditRepository auditRepository;
    private final MemberService memberService;

    public AuditService(AuditRepository auditRepository, MemberService memberService) {
        this.auditRepository = auditRepository;
        this.memberService = memberService;
    }

    @Loggable
    public List<Audit> findAudits(){
        Member authMember = memberService.getAuthenticationMember();

        if (!authMember.getAuthority().equals("Admin")) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_UNAUTHORIZED);
        }
        return auditRepository.findAll();
    }


    public List<Audit> findAuditsByDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        Member authMember = memberService.getAuthenticationMember();
        checkAdminAuthority(authMember);

        return auditRepository.findByActivityTimeBetweenOrderByActivityTimeDesc(fromDate, toDate);
    }
    public List<Audit> findAuditsByLoginIdAndDateRange(String LoginId, LocalDateTime fromDate, LocalDateTime toDate) {
        Member authMember = memberService.getAuthenticationMember();
        checkAdminAuthority(authMember);

        return auditRepository.findByLoginIdAndActivityTimeBetweenOrderByActivityTimeDesc(LoginId, fromDate, toDate);
    }
    private void checkAdminAuthority(Member authMember) {
        if (!authMember.getAuthority().equals("Admin")) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_UNAUTHORIZED);
        }
    }

}
