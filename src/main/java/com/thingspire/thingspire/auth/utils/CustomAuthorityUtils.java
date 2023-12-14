package com.thingspire.thingspire.auth.utils;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

// SpringSecurity member의 권한 정보를 저장하거나 알아내는 클래스
@Component
public class CustomAuthorityUtils {

//    @Value("${mail.address.admin}")
//    private String adminMailAddress;

    private final List<GrantedAuthority> ADMIN_ROLES = AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER");

    private final List<GrantedAuthority> USER_ROLES = AuthorityUtils.createAuthorityList("ROLE_USER");

    private final List<String> ADMIN_ROLES_STRING = List.of("ADMIN", "USER");
    private final List<String> USER_ROLES_STRING = List.of("USER");

    //DB저장용
    public List<String> createRoles(String authority) {
        if (authority.equals("Admin")) {
            return ADMIN_ROLES_STRING;
        }
        return USER_ROLES_STRING;
    }

    //TODO : 다음주에 고칠것.
    public List<GrantedAuthority> createAuthorities(List<String> roles) {
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role)) // (2)
                .collect(Collectors.toList());
        return authorities;
    }
}
