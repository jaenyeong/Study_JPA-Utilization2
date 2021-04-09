package com.jaenyeong.jpabook.jpashop.api;

import com.jaenyeong.jpabook.jpashop.domain.Member;
import com.jaenyeong.jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid final Member member) {
        final Long id = memberService.join(member);

        return new CreateMemberResponse(id);
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid final CreateMemberRequest request) {
        final Member member = new Member();
        member.setName(request.getName());

        final Long id = memberService.join(member);

        return new CreateMemberResponse(id);
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") final Long id, @RequestBody @Valid UpdateMemberRequest request) {
        memberService.update(id, request.getName());
        final Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result<List<MemberDto>> membersV2() {
        final List<Member> findMembers = memberService.findMembers();
        final List<MemberDto> memberDtos = findMembers.stream()
            .map(member -> new MemberDto(member.getName()))
            .collect(Collectors.toList());

        return new Result<>(memberDtos.size(), memberDtos);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int size;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }

    @Data
    static class CreateMemberRequest {
        @NotEmpty
        private String name;
    }

    @Data
    @RequiredArgsConstructor
    static class CreateMemberResponse {
        private final Long id;
    }

    @Data
    static class UpdateMemberRequest {
        @NotEmpty
        private String name;
    }

    @Data
    @RequiredArgsConstructor
    static class UpdateMemberResponse {
        private final Long id;
        private final String name;
    }
}
