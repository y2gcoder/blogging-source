package com.y2gcoder.learningcode.pageablerequest.application;

import com.y2gcoder.learningcode.pageablerequest.domain.Member;
import com.y2gcoder.learningcode.pageablerequest.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final DateTimeHolder dateTimeHolder;

    @Transactional
    public void join(String name) {
        Member member = Member.builder()
                .name(name)
                .joinedAt(dateTimeHolder.currentTime())
                .build();
        memberRepository.save(member);
    }

    public Page<MemberDto> findAll(Pageable pageable) {
        return memberRepository.findAll(pageable).map(MemberDto::new);
    }
}
