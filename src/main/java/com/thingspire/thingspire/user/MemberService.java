package com.thingspire.thingspire.user;

import com.thingspire.thingspire.audit.Loggable;
import com.thingspire.thingspire.auth.utils.CustomAuthorityUtils;
import com.thingspire.thingspire.exception.BusinessLogicException;
import com.thingspire.thingspire.exception.ExceptionCode;
import com.thingspire.thingspire.user.dto.MemberDTO;
import com.thingspire.thingspire.user.repository.MemberRepository;
import lombok.Getter;
import org.apache.poi.sl.draw.geom.GuideIf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class MemberService {
    private final Logger log = LoggerFactory.getLogger(MemberService.class);
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthorityUtils authorityUtils;

    // 생성자 DI
    public MemberService(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder,
            CustomAuthorityUtils authorityUtils)
    {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityUtils = authorityUtils;
    }
    @Getter
    @Value("${password.reset}")
    private String resetPassword;

    @Loggable
    /*
        회원 생성

        헤더에 토큰값이 있는 관리자만 회원 생성 가능.
        로그인 아이디 중복 시. 이메일 중복 시. 생성자가 관리자 권한이 아닐 시.
        Audit 실패 log 기록

        DTO 단에서의 에러는...?
    */
    public Member createMember(Member member) {

        // 로그인 아이디 중복 검사. 이메일 중복 검사. 관리자 권한 검사
        verifyExistLoginId(member.getLoginId());
        verifyExistEmail(member.getEmail());

        // 로그인 사용자 정보 가져오기
        Member authMember = getAuthenticationMember();
        member.setCreatedBy(authMember.getName());
        member.setModifiedBy(authMember.getName());
        isAdmin(authMember.getAuthority());

        member.setPassword(passwordEncoder.encode(member.getPassword()));
        List<String> roles = authorityUtils.createRoles(member.getAuthority());

        member.setDepartmentType(setDepartmentType(member.getDepartment())); // Type
        member.setFactoryCode(returnFactoryCode(member.getDepartment())); // factory code

        member.setRoles(roles);
        member.setActivated(true);

        // 회원 가입에 성공하면 로그 출력
        log.info("###############################################");
        log.info("### 계정 명 :{} 님 께서 회원가입 하셨습니다. ###",member.getEmail());
        log.info("### 해당 계정( {} )은 {} 권한입니다. ###",member.getEmail(), member.getRoles().get(0));
        log.info("###############################################");

        return memberRepository.save(member);
    }

    /*
        회원 정보 수정

        관리자 혹은 사용자의 회원 정보 수정
        부서, 전화번호 변경 가능
    */
    @Loggable
    public Member updateMember(Member member){
        Member findMember = findVerifiedMember(member.getMemberId());

        // 로그인 한 정보
        Member authMember = getAuthenticationMember();

        if (authMember.getAuthority().equals("User")) { // 사용자 정보 수정
            Optional.ofNullable(member.getPhoneNumber())
                    .ifPresent(phoneNumber -> findMember.setPhoneNumber(phoneNumber)); // 전화번호
            Optional.ofNullable(member.getLoginId())
                            .ifPresent(loginId->findMember.setLoginId(loginId)); // 로그인 아이디
            Optional.ofNullable(member.getEmail())
                    .ifPresent(email->findMember.setEmail(email)); // 이메일

            findMember.setModifiedBy(authMember.getName());
        }
        else{ // 관리자 수정
            Optional.ofNullable(member.getDepartment())
                    .ifPresent(department -> { // 회사명
                        findMember.setDepartment(department);
                        String departmentType = setDepartmentType(department);
                        findMember.setDepartmentType(departmentType);
                        String factoryCode = returnFactoryCode(department);
                        findMember.setFactoryCode(factoryCode);
                    });
            Optional.ofNullable(member.getLoginId())
                    .ifPresent(loginId->findMember.setLoginId(loginId)); // 로그인 아이디
            Optional.ofNullable(member.getAuthority())
                    .ifPresent(authority->findMember.setAuthority(authority)); // 권한
            Optional.ofNullable(member.getName())
                    .ifPresent(name->findMember.setName(name)); // 이름
            Optional.ofNullable(member.getPhoneNumber())
                    .ifPresent(phoneNumber -> findMember.setPhoneNumber(phoneNumber)); // 전화번호
            Optional.ofNullable(member.getEmail())
                    .ifPresent(email -> findMember.setEmail(email)); // 이메일

            findMember.setModifiedBy(authMember.getName());

        }
        if((findMember.getMemberId() == authMember.getMemberId()) || authMember.getAuthority().equals("Admin")){
            return memberRepository.save(findMember);
//            return findMember;
        }else{
            throw new BusinessLogicException(ExceptionCode.MEMBER_UNAUTHORIZED);
        }

    }

    /*
        회원 삭제

        관리자의 사용자 삭제
        헤더토큰에 관리자 권한이 있는 사람만 삭제 가능하다.
        조회한 회원 ID가 유효하지 않을 시. 사용자가 삭제할 시.
        Audit 실패 log 기록.
     */
    @Loggable
    public void deleteMember(long memberId) {

        Member findMember = findVerifiedMember(memberId);
        Member LoginMember = getAuthenticationMember();
        // 로그인 인증한 사람과 조회하려는 사람이 같아야한다. 관리자는 모두 가능
        if(findMember.getMemberId() == LoginMember.getMemberId() || LoginMember.getAuthority().equals("Admin")){
            memberRepository.delete(findMember);
        }else{
            throw new BusinessLogicException(ExceptionCode.MEMBER_UNAUTHORIZED);
        }
    }

    /*
        회원 다중 삭제
        체크박스로 표시한 회원 여러명 한꺼번에 삭제!
        헤더토큰에 관리자 권한이 있는 사람만 삭제 가능하다.
        조회한 회원 ID들이 유효하지 않거나, 사용자(User)가 삭제하면 에러
    */
    @Loggable
    public void deleteMembers(List<Long>memberIds) {
        Member loginMember = getAuthenticationMember();

        if (loginMember.getAuthority().equals("Admin")) { // 관리자 권한
            for (Long memberId : memberIds) {
                try {
                    memberRepository.deleteById(memberId);
                } catch (EmptyResultDataAccessException e) {
                    throw new BusinessLogicException(ExceptionCode.MEMBERID_NOT_VALID);
                }
            }
        } else {
            throw new BusinessLogicException(ExceptionCode.MEMBER_UNAUTHORIZED);
        }

    }
    /*
        회원 개별 조회 기능

        헤더에 토큰값이 있고 사용자는 자신의 정보만. 관리자는 모두 조회 가능
        ID 잘못됐을 경우. 다른 ID를 조회할 경우. Audit 실패 log 기록

    */
    //@Loggable - 사용자 개별 조회 Audit 남기기 X
    public Member findMemberByMemberID(long memberId) {

        Member findMember = findVerifiedMember(memberId);
        Member LoginMember = getAuthenticationMember();
        // 로그인 인증한 사람과 조회하려는 사람이 같아야한다. 관리자는 모두 가능
        if((findMember.getMemberId() == LoginMember.getMemberId()) || LoginMember.getAuthority().equals("Admin")){
            return findMember;
        }else{
            throw new BusinessLogicException(ExceptionCode.MEMBER_UNAUTHORIZED);
        }
    }

    /*
        회원 전체 조회

        관리자 권한만 전체 조회 가능
        사용자 조회시 Audit 실패 에러 기록
    */
    //@Loggable -- 사용자 전체 조회 Audit 남기기 X
    public List<Member> findMembers(){

        // 로그인 인증한 사용자 정보 가져오기
        Member authMember = getAuthenticationMember();

        if (!authMember.getAuthority().equals("Admin")) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_UNAUTHORIZED);
        }
        return memberRepository.findAll();
    }

    /*
        비밀번호 변경(Patch)

        관리자는 모두, 회원은 자신의 비밀번호만 변경할 수 있다.
    */
    @Loggable
    public Member patchPassword(MemberDTO.PatchPassword memberPatchPasswordDTO) {

        Member findMember = findVerifiedMember(memberPatchPasswordDTO.getMemberId());
        verifyPassword(memberPatchPasswordDTO.getNewPassword(), memberPatchPasswordDTO.getPasswordConfirm());
        Member loginMember = getAuthenticationMember();
        if(loginMember.getMemberId() == findMember.getMemberId() || loginMember.getAuthority().equals("Admin")){
            String encodePassword = passwordEncoder.encode(memberPatchPasswordDTO.getNewPassword());
            findMember.setPassword(encodePassword);
            return memberRepository.save(findMember);
        } else throw new BusinessLogicException(ExceptionCode.MEMBER_UNAUTHORIZED);
    }

    /*
        비밀번호 초기화(Reset)

        비밀번호를 몰라서 로그인을 하지 못하는 사람을 위한 서비스
        로그인을 하지 않아도 되기 때문에 JWT 필요없다.

        비밀번호가 없는 대신 이메일과 로그인 아이디를 가져오기로 한다.
        자신의 정보만 수정 가능
    */
    @Loggable
    public Member resetPassword(MemberDTO.ResetPassword memberResetPasswordDTO) {

        findVerifiedMemberId(memberResetPasswordDTO.getMemberId());
        Member findMember = findVerifiedMember(memberResetPasswordDTO.getMemberId()); // memberId로 정보 가져오기
        verifyValidLoginId(memberResetPasswordDTO.getLoginId()); // 유효한 로그인아이디?
        verifyValidEmail(findMember.getEmail()); // 유효한 이메일?
        // 이메일을 잘못 작성한 경우
        if(!findMember.getEmail().equals(memberResetPasswordDTO.getEmail())) throw new BusinessLogicException(ExceptionCode.WRONG_EMAIL_ADDRESS);
        findMember.setPassword( passwordEncoder.encode(resetPassword));
        return memberRepository.save(findMember);
    }





    // set factoryCode
    public String returnFactoryCode(String department) {
        if(department.equals("아이지스")) return "igs";
        else if(department.equals("아이코디")) return "icd";
        else if(department.equals("디알텍")) return "drt";
        else if(department.equals("DH 글로벌")) return "dhg";
        else if(department.equals("재원산업(주)")) return "jw";
        else if(department.equals("(주)에스에프시")) return "sfc";
        else return "jg"; // 진곡
    }

    // Valid Member Check Method
    public Member findVerifiedMember(long memberId) { // memberId로 존재하는 회원인지 찾기
        Optional<Member> optionalMember = memberRepository.findById(memberId);

        return optionalMember.orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.MEMBERID_NOT_VALID));
    }

    public Member findVerifiedMemberByLoginId(String loginId) { // 로그인 아이디로 존재하는 회원인지 찾기
        Optional<Member> optionalMember = memberRepository.findByLoginId(loginId);
        return optionalMember.orElseThrow(()-> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    }


    public void findVerifiedMemberId(long memberId) {  // 유효한 회원 ID인지 검사
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        if(!optionalMember.isPresent()) throw new BusinessLogicException(ExceptionCode.MEMBERID_NOT_VALID);
    }

    public void verifyValidLoginId(String loginId) {
        Optional<Member> optionalMember = memberRepository.findByLoginId(loginId);
        if(!optionalMember.isPresent()) throw new BusinessLogicException(ExceptionCode.LOGIN_ID_FAILED);
    }

    private void verifyExistEmail(String email) { // 중복 이메일인지 검사
        Optional<Member> optionalMember = memberRepository.findByEmail(email);
        if(optionalMember.isPresent()) throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);
    }

    // 이메일(중복 x)로 회원 가져올 수 있는지 조회
    private void verifyValidEmail(String email) {
        Optional<Member> optionalMember = memberRepository.findByEmail(email);
        if(!optionalMember.isPresent()) throw new BusinessLogicException(ExceptionCode.Email_NOT_VALID); // 이메일 not found로 바꿀 것..!
    }

    private void verifyExistLoginId(String loginId) { // 중복 아이디인지 검사
        Optional<Member> optionalMember = memberRepository.findByLoginId(loginId);
        if(optionalMember.isPresent()) throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);
    }

    // 로그인한 사용자 객체정보 가져오기, 로그인 안했으면 예외발생
    public Member getAuthenticationMember(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(!auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken){
            throw new BusinessLogicException(ExceptionCode.MEMBER_UNAUTHORIZED);
        }
        Member authorizedMember = findVerifiedMemberByLoginId(auth.getName());
        return authorizedMember;
    }

    private void verifyPassword(String password1, String password2) {
        if(!password1.equals(password2)) throw new BusinessLogicException(ExceptionCode.PASSWORD_NOT_SAME);
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }

    // 관리자인지 true/false
    private void isAdmin(String authority) {
        if(authority.equals("Admin")) return;
        throw new BusinessLogicException(ExceptionCode.MEMBER_UNAUTHORIZED);
    }

    // 회사명 type set
    private String setDepartmentType(String department) {
        if(department.equals("진곡")) return "solar";
        else return "elec";
    }
}
