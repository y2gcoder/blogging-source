package com.y2gcoder.learningcode.pageablerequest.infrastructure;

import com.y2gcoder.learningcode.pageablerequest.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
