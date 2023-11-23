package com.thingspire.thingspire.auth.service;

import com.thingspire.thingspire.auth.utils.CustomAuthorityUtils;
import com.thingspire.thingspire.exception.BusinessLogicException;
import com.thingspire.thingspire.exception.ExceptionCode;
import com.thingspire.thingspire.user.Member;
import com.thingspire.thingspire.user.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
@Slf4j

// DB 사용자 조회 후 AuthenticationManeger에게 전달하는 클래스
public class MemberDetailService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final CustomAuthorityUtils authorityUtils;

    public MemberDetailService(MemberRepository memberRepository, CustomAuthorityUtils authorityUtils) {
        this.memberRepository = memberRepository;
        this.authorityUtils = authorityUtils;
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Optional<Member> optionalMember = memberRepository.findByLoginId(loginId);
        Member findMember = optionalMember.orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));

        // 아이디 오류
        if(!findMember.getLoginId().equals(loginId)){
            throw new BusinessLogicException(ExceptionCode.LOGIN_ID_FAILED);
        } return new MemberDetails(findMember);
    }

    //MemberDetails 클래스 추가
    //UserDetails 인터페이스를 구현하고 있고 또한 Member 엔티티 클래스를 상속.
    //이렇게 구성하면 데이터베이스에서 조회한 회원 정보를 Spring Security의 User 정보로 변환하는 과정과
    //User의 권한 정보를 생성하는 과정을 캡슐화
    public final class MemberDetails extends Member implements UserDetails {
        MemberDetails(Member member) {
            setMemberId(member.getMemberId());
            setLoginId(member.getLoginId());
            setPhoneNumber(member.getPhoneNumber());
            setName(member.getName());
            setEmail(member.getEmail());
            setPassword(member.getPassword());
            setRoles(member.getRoles());
            setDepartmentType(member.getDepartmentType());
            setDepartment(member.getDepartment());
            setAuthority(member.getAuthority());
            setFactoryCode(member.getFactoryCode());

        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorityUtils.createAuthorities(this.getRoles());
        }

        @Override
        public String getUsername() {
            return getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        public String getLoginId(){return super.getLoginId();}
        public String getName(){return super.getName();}
    }

}
