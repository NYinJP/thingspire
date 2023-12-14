package com.thingspire.thingspire.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberResponseDTO {
    private MemberDTO.Response member;
    private String message;

    public void setMember(MemberDTO.Response member) {
        this.member = member;
    }
}
