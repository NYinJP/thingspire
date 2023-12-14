package com.thingspire.thingspire.audit;

import com.thingspire.thingspire.exception.BusinessLogicException;
import com.thingspire.thingspire.exception.ExceptionCode;
import com.thingspire.thingspire.user.Member;
import com.thingspire.thingspire.user.dto.MemberDTO;
import com.thingspire.thingspire.user.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Aspect
@Component
@Slf4j
public class AuditAspect {
    @Autowired
    private AuditRepository auditRepository;
    @Autowired
    private MemberRepository memberRepository;

    // 로그인 이후 로그 남기기
    // 메서드가 성공적으로 실행되고 결괏값을 반환한 후 적용할 어드바이스

    @AfterReturning("@annotation(Loggable)")
    public void logActivity(JoinPoint joinPoint) {

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getName();
        String fullMethodName = className + "." + methodName;

        String detailed = getActivityTypeFromMethodName(methodName);
        String[] detailedInfo = detailed.split("/");

        // 로그 작업
        log.info("##### Method executed: {}", fullMethodName);

        // 로그인한 사용자의 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Member member;
        Audit audit = new Audit();

        // 만약 로그인이 필요하지 않은 요청이라면
        if (!authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {

            Object[] args = joinPoint.getArgs();

            // 만약 resetPassword 메서드의 JointPoint라면(비밀번호 초기화)
            if (args != null && args.length > 0 && args[0] instanceof MemberDTO.ResetPassword) {
                MemberDTO.ResetPassword memberResetPasswordDTO = (MemberDTO.ResetPassword) args[0];
                // DTO 정보를 가져와 Audit 테이블에 저장하는 로직을 여기에 작성합니다.
                //audit.setLoginId(memberResetPasswordDTO.getLoginId());
                //Member findMember = memberRepository.findByLoginId(memberResetPasswordDTO.getLoginId()).orElse(null);
                //audit.setName(findMember.getName());
                //audit.setName(memberResetPasswordDTO.getLoginId());

            }else{ // 기타 다른 요청
                audit.setLoginId("비로그인 사용자");
                audit.setName("비로그인 사용자");
            }
        }else{ // 로그인이 필요한 요청
            member = memberRepository.findByLoginId(authentication.getName()).orElseThrow(()-> new BusinessLogicException(ExceptionCode.MEMBER_UNAUTHORIZED));
            audit.setLoginId(member.getLoginId());
            audit.setName(member.getName());
        }
        // 실행 메서드 명
        audit.setActivityType(detailedInfo[0]);
        audit.setDetail(detailedInfo[1]);
        audit.setActivityTime(LocalDateTime.now());

        // 로그인한 사용자의 정보 설정
        audit.setSuccess("성공");
        auditRepository.save(audit);
    }


    // 실패 로그 남기기
    // 대상 메서드에서 예외가 발생했을 때 적용할 어드바이스로 try/catch문에서의 catch와 비슷한 역할

    @AfterThrowing(pointcut = "@annotation(ErrorLoggable)", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable  ex) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getName();
        String fullMethodName = className + "." + methodName;

        String detailed = getActivityTypeFromMethodName(methodName);
        String[] detailedInfo = detailed.split("/");

        log.info("##### Method executed: {}", fullMethodName); // 실행 메서드 확인

        // Audit 객체 생성
        Audit audit = new Audit();

        Member member;

        // 로그인한 사용자의 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 만약 로그인을 하지 않는 요청이라면
        if (!authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {

            Object[] args = joinPoint.getArgs(); // 현재 실행 중인 메서드에 전달된 인자(매개변수) 배열 반환 메서드

            // 만약 resetPassword 메서드가 수행된거라면
            if (methodName.contains("resetPasswordOnlyMemberId")) {
                audit.setLoginId("관리자");
                audit.setName("관리자");
            }else {
                audit.setLoginId("비로그인 사용자");
                audit.setName("비로그인 사용자");
            }
            // 그 외의 경우? 는 잘 모르겠다.
            // ErrorLoggable 붙은 애 중에 아닌 애 찾아볼 것.


//            if (args != null && args.length > 0 && args[0] instanceof MemberDTO.ResetPassword) {
//                MemberDTO.ResetPassword memberResetPasswordDTO = (MemberDTO.ResetPassword) args[0];
//                // DTO 정보를 가져와 Audit 테이블에 저장하는 로직을 여기에 작성합니다 :: 📝고민중
//
////                audit.setLoginId(memberRepository.findById(memberResetPasswordDTO.getMemberId()).orElse(null).getLoginId());
////                audit.setName(memberRepository.findById(memberResetPasswordDTO.getMemberId()).orElse(null).getName());
//                //audit.setLoginId(memberResetPasswordDTO.getLoginId());
//                //audit.setName(memberResetPasswordDTO.getLoginId());

        }

        // 로그인을 받는 요청이라면
        else {
            member = memberRepository.findByLoginId(authentication.getName()).orElseGet(()->{
                return null;
            });
            audit.setLoginId(member != null ? member.getLoginId():null);
            audit.setName(member != null?member.getName():null);
        }

        // 실행 메서드 명
        audit.setActivityType(detailedInfo[0]);
        audit.setDetail(detailedInfo[1]);
        audit.setActivityTime(LocalDateTime.now());

        audit.setSuccess("실패");

        try {
            auditRepository.save(audit);
        } catch (Exception e) {
            log.error("Error occurred while saving audit: ", e);
        }
    }

    private String getActivityTypeFromMethodName(String methodName) {
        if (methodName.contains("createMember")) {
            return "계정관리/사용자 생성";
        } else if (methodName.contains("updateMember")) {
            return "계정관리/사용자 수정";
        } else if(methodName.contains("successfulAuthentication")){
            return "로그인/사용자 로그인";
        } else if(methodName.contains("deleteMember")){
            return "계정관리/사용자 삭제";
        } else if(methodName.contains("findMembers")){
            return "계정관리/사용자 전체 조회";
        } else if(methodName.contains("findMemberByMemberID")){
            return "계정관리/사용자 개별 조회";
        } else if(methodName.contains("delegateAccessToken")){
            return "로그인/사용자 로그인";
        } else if(methodName.contains("findAuditsByDateRange")){
            return "계정관리/Audit 관리 조회";
        } else if (methodName.contains("patchPassword")) {
            return "계정관리/비밀번호 변경";
        } else if (methodName.contains("resetPassword")) {
            return "계정관리/비밀번호 초기화";
        }
        // 그 외 경우
        else {
            return "알 수 없는 활동/알 수 없는 활동";
        }
    }

    private String extractRequestBody(HttpServletRequest request) {
        try {
            return request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            // requestBody 추출 실패 시 예외 처리
            log.error("Failed to extract requestBody: {}", e.getMessage());
            return null;
        }
    }
}

