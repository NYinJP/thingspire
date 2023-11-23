package com.thingspire.thingspire.user.mapper;

import com.thingspire.thingspire.user.Member;
import com.thingspire.thingspire.user.dto.MemberDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    Member memberPostDTOToMember(MemberDTO.Post memberDto);
    Member memberPatchDTOToMember(MemberDTO.Patch memberDTO);
    MemberDTO.Response memberToMemberResponseDTO(Member member);
    List<MemberDTO.Response> memberToMemberResponseDTOs(List<Member> members);

}
