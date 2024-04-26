package com.y2gcoder.learningcode.pageablerequest.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.y2gcoder.learningcode.pageablerequest.domain.Member;
import com.y2gcoder.learningcode.pageablerequest.infrastructure.MemberRepository;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class MemberServiceTest {

    @Autowired
    private MemberService sut;

    @Autowired
    private MemberRepository memberRepository;

    @MockBean
    private DateTimeHolder dateTimeHolder;

    @Transactional
    @DisplayName("회원가입 할 수 있다.")
    @Test
    void join() {
        // given
        Instant currentTime = Instant.ofEpochSecond(0);
        BDDMockito.given(dateTimeHolder.currentTime()).willReturn(currentTime);

        String name = "name";

        // when
        sut.join(name);

        // then
        List<Member> members = memberRepository.findAll();
        assertThat(members).hasSize(1);
        assertThat(members).extracting(Member::getJoinedAt)
                .containsExactlyInAnyOrder(currentTime);
        assertThat(members).extracting(Member::getName)
                .containsExactlyInAnyOrder(name);
    }

    @Transactional
    @DisplayName("회원가입한 유저 목록을 조회할 수 있다: 1페이지, 10명")
    @Test
    void findAllWithPageablePage0Size10() {
        // given
        List<Member> members = IntStream.rangeClosed(1, 10000).mapToObj(i -> Member.builder()
                        .name("name" + i)
                        .joinedAt(Instant.ofEpochSecond(i))
                        .build())
                .toList();
        memberRepository.saveAll(members);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<MemberDto> results = sut.findAll(pageable);

        // then
        assertThat(results.getTotalElements()).isEqualTo(10000);
        assertThat(results.getTotalPages()).isEqualTo(1000);
        assertThat(results.getSize()).isEqualTo(10);
        assertThat(results.getNumber()).isEqualTo(0);
        assertThat(results.hasNext()).isTrue();
        assertThat(results.hasPrevious()).isFalse();
        assertThat(results.getContent()).extracting(MemberDto::name)
                .containsExactlyInAnyOrder(IntStream.rangeClosed(1, 10).mapToObj(i -> "name" + i)
                        .toArray(String[]::new));
        assertThat(results.getContent()).extracting(MemberDto::joinedAt)
                .containsExactlyInAnyOrder(IntStream.rangeClosed(1, 10).mapToObj(
                                Instant::ofEpochSecond)
                        .toArray(Instant[]::new));
    }
}