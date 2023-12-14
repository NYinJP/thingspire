package com.thingspire.thingspire.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {
    private static final Logger log = LoggerFactory.getLogger(SecurityUtils.class);
    private SecurityUtils() {

    }
    public static Optional<Long> getCurrentMemberId() {


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof MemberDetailService.MemberDetails) {
            MemberDetailService.MemberDetails userDetails = (MemberDetailService.MemberDetails) authentication.getPrincipal();
            return Optional.ofNullable(userDetails.getMemberId());
        }

        return null;
    }
}
