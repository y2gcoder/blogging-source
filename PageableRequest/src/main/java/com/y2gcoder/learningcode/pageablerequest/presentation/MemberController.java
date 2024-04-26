package com.y2gcoder.learningcode.pageablerequest.presentation;

import com.y2gcoder.learningcode.pageablerequest.application.MemberDto;
import com.y2gcoder.learningcode.pageablerequest.application.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/members")
    public PageResponse<MemberDto> findAll(@PageableDefault Pageable pageable) {
        Page<MemberDto> results = memberService.findAll(pageable);
        return new PageResponse<>(results);
    }
}
