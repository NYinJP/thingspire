package com.thingspire.thingspire.advice;

import com.thingspire.thingspire.audit.Audit;
import com.thingspire.thingspire.audit.AuditRepository;
import com.thingspire.thingspire.exception.BusinessLogicException;
import com.thingspire.thingspire.exception.PasswordInputException;
import com.thingspire.thingspire.response.ErrorResponse;
import com.thingspire.thingspire.user.Member;
import com.thingspire.thingspire.user.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    @Autowired
    AuditRepository auditRepository;
    @Autowired
    MemberService memberService;

    // DTO 제약조건에서 발생하는 에러 처리
    @ExceptionHandler
    public ResponseEntity handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        Member authMember = memberService.getAuthenticationMember();

        Audit audit = new Audit();
        audit.setLoginId(authMember.getLoginId());
        audit.setName(authMember.getName());
        audit.setActivityTime(LocalDateTime.now());
        audit.setActivityType("계정관리");
        audit.setDetail("사용자 생성");
        audit.setSuccess("실패");
        auditRepository.save(audit);

        List<ErrorResponse.FieldError> errors = ErrorResponse.FieldError.of(e.getBindingResult());
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler
    public ResponseEntity handleConstraintViolationException(ConstraintViolationException e) {

//        Member authMember = memberService.getAuthenticationMember();
//
//        Audit audit = new Audit();
//        audit.setLoginId(authMember.getLoginId());
//        audit.setName(authMember.getName());
//        audit.setActivityTime(LocalDateTime.now());
//        audit.setActivityType("계정관리");
//        audit.setDetail("사용자 생성");
//        audit.setSuccess("실패");
//        auditRepository.save(audit);

        List<ErrorResponse.ConstraintViolationError > errors = ErrorResponse.ConstraintViolationError.of(e.getConstraintViolations());
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ErrorResponse> handleBusinessLogicException(BusinessLogicException e) {
        System.out.println("에러 메시지입니다 : " + e.getExceptionCode().getMessage());
        final ErrorResponse response = ErrorResponse.of(e.getExceptionCode());

        return new ResponseEntity<>(response, HttpStatus.valueOf(e.getExceptionCode()
                .getStatus()));
    }

    @ExceptionHandler(PasswordInputException.class)
    public ResponseEntity<List<ErrorResponse>> handlePasswordInputExcepgion(PasswordInputException e) {
        System.out.println("에러 메시지입니다 : " + e.getExceptionCode().getMessage());
        final ErrorResponse response = ErrorResponse.of(e.getExceptionCode());
        List<ErrorResponse> responseList = Collections.singletonList(response);

        return new ResponseEntity<>(responseList, HttpStatus.valueOf(e.getExceptionCode().getStatus()));
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponse handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {

        final ErrorResponse response = ErrorResponse.of(HttpStatus.METHOD_NOT_ALLOWED);

        return response;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {

        final ErrorResponse response = ErrorResponse.of(HttpStatus.BAD_REQUEST,
                "Required request body is missing");

        return response;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {

        final ErrorResponse response = ErrorResponse.of(HttpStatus.BAD_REQUEST,
                e.getMessage());

        return response;
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        System.out.println("에러 메시지입니다 : " + e.getMessage());
        final ErrorResponse response = ErrorResponse.of(HttpStatus.UNAUTHORIZED,e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

//    @ExceptionHandler(AuthenticationException.class)
//    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
//        System.out.println("에러 메시지입니다 : " + e.getMessage());
//        final ErrorResponse response = ErrorResponse.of(HttpStatus.UNAUTHORIZED,e.getMessage());
//
//        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
//    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorResponse> handleAuthenticationException2(AuthenticationException e) {
        String errorMessage = e.getMessage(); // 예외에서 메시지 가져오기
        log.warn("Authentication failed: {}", errorMessage);

        ErrorResponse response = ErrorResponse.of(HttpStatus.UNAUTHORIZED, errorMessage);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

}