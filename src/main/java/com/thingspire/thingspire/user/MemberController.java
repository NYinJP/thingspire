package com.thingspire.thingspire.user;

import com.thingspire.thingspire.response.ErrorResponse;
import com.thingspire.thingspire.user.dto.MemberDTO;
import com.thingspire.thingspire.user.mapper.MemberMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Validated
public class MemberController {
    private final Logger log = LoggerFactory.getLogger(MemberController.class);
    private final MemberService memberService;
    private final MemberMapper mapper;

    public MemberController(MemberService memberService,
                            MemberMapper mapper) {
        this.memberService = memberService;
        this.mapper = mapper;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity createMember(@Valid @RequestBody MemberDTO.Post requestBody) {
        if (requestBody == null) {
            System.out.println("null값 입니다!!!!!!!!!!!!!!!");
        }


        Member newMember = memberService.createMember(mapper.memberPostDTOToMember(requestBody));

        URI uri = UriComponentsBuilder.newInstance()
                .path("/api/users/" + newMember.getMemberId())
                .build().toUri();

        return ResponseEntity.created(uri).build();
    }

    // 회원 단일 삭제
    @DeleteMapping("/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ErrorResponse> deleteMember(@PathVariable @Positive long memberId) {
        memberService.deleteMember(memberId);
        ErrorResponse response = ErrorResponse.of(HttpStatus.OK, "회원 삭제가 성공했습니다.");

        return ResponseEntity.ok(response);
    }


    // 회원 다중 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<ErrorResponse> deleteMembers(@RequestParam List<Long> memberIds) {
        memberService.deleteMembers(memberIds);
        ErrorResponse response = ErrorResponse.of(HttpStatus.OK,"운영자 삭제가 성공했습니다.");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity getMember(@PathVariable @Positive long memberId) {
        Member member = memberService.findMemberByMemberID(memberId);
        return new ResponseEntity(mapper.memberToMemberResponseDTO(member), HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity getMembers() {
        List<Member> members = memberService.findMembers();
        return new ResponseEntity(mapper.memberToMemberResponseDTOs(members), HttpStatus.OK);
    }

    @PatchMapping("/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity updateMember(@PathVariable("memberId") @Positive long memberId,
                                       @Valid @RequestBody MemberDTO.Patch requestBody) {
        requestBody.setMemberId(memberId);
        Member member = memberService.updateMember(mapper.memberPatchDTOToMember(requestBody));
        return new ResponseEntity(mapper.memberToMemberResponseDTO(member), HttpStatus.OK);
    }

    @PatchMapping("/password/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity updatePassword(@PathVariable("memberId") @Positive long memberId, @Valid @RequestBody MemberDTO.PatchPassword requestBody) {
        requestBody.setMemberId(memberId);
        Member patchMember = memberService.patchPassword(requestBody);

        URI uri = UriComponentsBuilder.newInstance()
                .path("/api/users" + patchMember.getMemberId())
                .build().toUri();

        return ResponseEntity.created(uri).build();
    }
    @PatchMapping("/reset/password/{memberId}")
    public ResponseEntity resetPassword(@PathVariable("memberId") @Positive long memberId, @Valid @RequestBody MemberDTO.ResetPassword requestBody) {
        requestBody.setMemberId(memberId);
        Member resetPasswordMember = memberService.resetPassword(requestBody);

        URI uri = UriComponentsBuilder.newInstance()
                .path("/api/users" + resetPasswordMember.getMemberId())
                .build().toUri();

        return ResponseEntity.created(uri).build();
    }
}
