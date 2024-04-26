package com.y2gcoder.learningcode.pageablerequest;

import com.y2gcoder.learningcode.pageablerequest.domain.Member;
import com.y2gcoder.learningcode.pageablerequest.infrastructure.MemberRepository;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberDataInit {
    private final MemberRepository memberRepository;

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        List<Member> members = IntStream.rangeClosed(1, 10000).mapToObj(i -> Member.builder()
                        .name("name" + i)
                        .joinedAt(Instant.ofEpochSecond(i))
                        .build())
                .toList();
        memberRepository.saveAll(members);
        log.info("Member Dummy Data Initialized");
    }

}
