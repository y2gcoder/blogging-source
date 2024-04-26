package com.y2gcoder.learningcode.pageablerequest.application;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.y2gcoder.learningcode.pageablerequest.domain.Member;
import java.time.Instant;

@JsonNaming(SnakeCaseStrategy.class)
public record MemberDto(
        long id,
        String name,
        Instant joinedAt
) {

    public MemberDto(Member entity) {
        this(entity.getId(), entity.getName(), entity.getJoinedAt());
    }
}
