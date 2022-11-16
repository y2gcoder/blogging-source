package com.blogging.scheduled.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TestScheduler3 {

	@Async
	@Scheduled(cron = "0/1 * * * * ?")
	public void schedule1() throws InterruptedException {
		log.info("schedule1, START, current thread: {}", Thread.currentThread().getName());
		Thread.sleep(5000);
		log.info("schedule1, END, current thread: {}", Thread.currentThread().getName());
	}
}
