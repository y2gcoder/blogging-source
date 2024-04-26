package com.y2gcoder.learningcode.pageablerequest.infrastructure;

import com.y2gcoder.learningcode.pageablerequest.application.DateTimeHolder;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SystemDateTimeHolder implements DateTimeHolder {

    @Override
    public Instant currentTime() {
        return Instant.now();
    }
}
