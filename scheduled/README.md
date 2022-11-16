# @Scheduled 사용하기 3분 전에 볼만한 글

예전 회사에서 했던 첫 프로젝트에서 자동 결제, 자동 해지 기능이 필요한 프로젝트가 있었다. 고객의 매월 결제일마다 사용하고 있는 PG의 자동결제 API 요청을 보내고, 
재시도 끝에 결제하지 못했을 때, 해당 사용자가 서비스를 이용할 수 없게 해지 처리를 해줘야 했다. 간단하고 빠르게 해당 기능을 추가해야 하는 상황이었기 때문에, 고민하다 Spring @Scheduled를 사용했다.

그 당시에는 @Scheduled에 대한 이해없이 첫 프로젝트라 해야한다는 압박감에 사용하기에만 급급했다. 그래서 @Scheduled의 작동원리나 실제 작동 방식에 대한 이해가 부족했다. 최근에 내가 했던 프로젝트들을 쭉 둘러보다 첫 프로젝트에서 @Scheduled 를 추가하면서 겪었던 시행착오가 생각나 후에 @Scheduled를 사용할 사람들을 위해 @Scheduled에 대해 정리하기로 했다.

## @Scheduled

@Scheduled를 사용하면 독립적인 쓰레드에서 TaskScheduler의 구현체에 실행할 콜백(Runnable, 여기서는 해당 애노테이션이 있는 메서드가 해당됩니다.)과 반복 주기(Trigger의 구현체, 애노테이션에서 설정했던 주기 옵션입니다.)를 넘겨 주기적인 작업을 시행합니다.

```java
public interface TaskScheduler {
	...
	@Nullable
	ScheduledFuture<?> schedule(Runnable task, Trigger trigger);
	...
}
```

## 설정

@Scheduled는 설정이 매우 쉽다.

```java

@EnableScheduling
@SpringBootApplication
public class ScheduledApplication {

  ...

}

```

먼저 `@EnableScheduling` 을 선언하고

```java
@Component
public class Example {
	@Scheduled(fixedDelay = 1000)
	public void schedule() {
          ...
	}
	
}
```

그 후 빈으로 등록된 클래스에서 스케줄링 작업을 사용하고자 하는 메서드에 `@Scheduled` 애노테이션을 붙여 사용할 수 있다.

## 테스트

`@Scheduled`를   

### 기본 설정에서 여러 개의 스케줄링 메서드를 실행하면? 

@Scheduled 기본적인 설정에서는 쓰레드가 1개인 쓰레드 풀을 생성해서 동기적으로 작업을 수행한다고 한다. 그렇다면 스케줄링 작업이 여러 개일 때 쓰레드가 1개라면 어떻게 동작할까?

> 편의를 위해 lombok의 @Slf4j를 이용해서 로깅했다.

```java
@Slf4j
@Component
public class TestScheduler1 {
	@Scheduled(fixedDelay = 1000)
	public void schedule1() throws InterruptedException {
		log.info("schedule1, START, current thread: {}", Thread.currentThread().getName());
		Thread.sleep(5000);
		log.info("schedule1, END, current thread: {}", Thread.currentThread().getName());
	}

	@Scheduled(fixedDelay = 1000)
	public void schedule2() {
		log.info("schedule2, START, current thread: {}", Thread.currentThread().getName());
		log.info("schedule2, END, current thread: {}", Thread.currentThread().getName());
	}
}
```

해당 스케줄러를 실행하면

```logcatfilter
2022-11-16 12:13:34.585  INFO 25897 --- [   scheduling-1] c.b.scheduled.scheduler.TestScheduler1   : schedule1, START, current thread: scheduling-1
2022-11-16 12:13:39.588  INFO 25897 --- [   scheduling-1] c.b.scheduled.scheduler.TestScheduler1   : schedule1, END, current thread: scheduling-1
2022-11-16 12:13:39.588  INFO 25897 --- [   scheduling-1] c.b.scheduled.scheduler.TestScheduler1   : schedule2, START, current thread: scheduling-1
2022-11-16 12:13:39.588  INFO 25897 --- [   scheduling-1] c.b.scheduled.scheduler.TestScheduler1   : schedule2, END, current thread: scheduling-1
2022-11-16 12:13:40.588  INFO 25897 --- [   scheduling-1] c.b.scheduled.scheduler.TestScheduler1   : schedule1, START, current thread: scheduling-1
2022-11-16 12:13:45.591  INFO 25897 --- [   scheduling-1] c.b.scheduled.scheduler.TestScheduler1   : schedule1, END, current thread: scheduling-1
2022-11-16 12:13:45.592  INFO 25897 --- [   scheduling-1] c.b.scheduled.scheduler.TestScheduler1   : schedule2, START, current thread: scheduling-1
2022-11-16 12:13:45.592  INFO 25897 --- [   scheduling-1] c.b.scheduled.scheduler.TestScheduler1   : schedule2, END, current thread: scheduling-1
```

역시나 schedule1 작업이 끝나야 schedule2가 실행된다. 그렇다면 일정 시간마다 작동하게 하는 cron은 어떨까?

```java
@Slf4j
@Component
public class TestScheduler2 {

	@Scheduled(cron = "0/1 * * * * ?")
	public void schedule1() throws InterruptedException {
		log.info("schedule1, START, current thread: {}", Thread.currentThread().getName());
		Thread.sleep(5000);
		log.info("schedule1, END, current thread: {}", Thread.currentThread().getName());
	}

	@Scheduled(cron = "0/1 * * * * ?")
	public void schedule2() {
		log.info("schedule2, START, current thread: {}", Thread.currentThread().getName());
		log.info("schedule2, END, current thread: {}", Thread.currentThread().getName());
	}
}

```

cron으로 1초마다 실행되게끔 했다.

```logcatfilter
2022-11-16 12:18:16.000  INFO 25981 --- [   scheduling-1] c.b.scheduled.scheduler.TestScheduler1   : schedule1, START, current thread: scheduling-1
2022-11-16 12:18:21.004  INFO 25981 --- [   scheduling-1] c.b.scheduled.scheduler.TestScheduler1   : schedule1, END, current thread: scheduling-1
2022-11-16 12:18:21.005  INFO 25981 --- [   scheduling-1] c.b.scheduled.scheduler.TestScheduler1   : schedule2, START, current thread: scheduling-1
2022-11-16 12:18:21.005  INFO 25981 --- [   scheduling-1] c.b.scheduled.scheduler.TestScheduler1   : schedule2, END, current thread: scheduling-1
2022-11-16 12:18:22.004  INFO 25981 --- [   scheduling-1] c.b.scheduled.scheduler.TestScheduler1   : schedule1, START, current thread: scheduling-1
2022-11-16 12:18:27.008  INFO 25981 --- [   scheduling-1] c.b.scheduled.scheduler.TestScheduler1   : schedule1, END, current thread: scheduling-1
2022-11-16 12:18:27.008  INFO 25981 --- [   scheduling-1] c.b.scheduled.scheduler.TestScheduler1   : schedule2, START, current thread: scheduling-1
2022-11-16 12:18:27.009  INFO 25981 --- [   scheduling-1] c.b.scheduled.scheduler.TestScheduler1   : schedule2, END, current thread: scheduling-1
```

역시나 쓰레드 1개로 2개의 작업을 수행 중이기 때문에 schedule1 작업이 끝난 후에 schedule2 작업을 시작하면서 앞 작업의 영향을 받는 것을 알 수 있다.

### 작업용 쓰레드를 늘린 상태에서 여러 개의 스케줄링 메서드를 실행하면?

쓰레드를 2개로 늘리면 어떻게 될까?

```java
@Configuration
public class SchedulerConfig implements SchedulingConfigurer {
	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(2);
		threadPoolTaskScheduler.setThreadGroupName("thread pool for scheduler test");
		threadPoolTaskScheduler.setThreadNamePrefix("my-test-thread-");
		threadPoolTaskScheduler.initialize();

		taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
	}
}
```

쓰레드를 2개로 늘리고, 보기 쉽게 prefix도 달아주었다. 

```logcatfilter
2022-11-16 12:28:28.003  INFO 26169 --- [y-test-thread-2] c.b.scheduled.scheduler.TestScheduler2   : schedule2, START, current thread: my-test-thread-2
2022-11-16 12:28:28.003  INFO 26169 --- [y-test-thread-1] c.b.scheduled.scheduler.TestScheduler2   : schedule1, START, current thread: my-test-thread-1
2022-11-16 12:28:28.005  INFO 26169 --- [y-test-thread-2] c.b.scheduled.scheduler.TestScheduler2   : schedule2, END, current thread: my-test-thread-2
2022-11-16 12:28:29.003  INFO 26169 --- [y-test-thread-2] c.b.scheduled.scheduler.TestScheduler2   : schedule2, START, current thread: my-test-thread-2
2022-11-16 12:28:29.004  INFO 26169 --- [y-test-thread-2] c.b.scheduled.scheduler.TestScheduler2   : schedule2, END, current thread: my-test-thread-2
2022-11-16 12:28:30.003  INFO 26169 --- [y-test-thread-2] c.b.scheduled.scheduler.TestScheduler2   : schedule2, START, current thread: my-test-thread-2
2022-11-16 12:28:30.004  INFO 26169 --- [y-test-thread-2] c.b.scheduled.scheduler.TestScheduler2   : schedule2, END, current thread: my-test-thread-2
2022-11-16 12:28:31.003  INFO 26169 --- [y-test-thread-2] c.b.scheduled.scheduler.TestScheduler2   : schedule2, START, current thread: my-test-thread-2
2022-11-16 12:28:31.004  INFO 26169 --- [y-test-thread-2] c.b.scheduled.scheduler.TestScheduler2   : schedule2, END, current thread: my-test-thread-2
2022-11-16 12:28:32.005  INFO 26169 --- [y-test-thread-2] c.b.scheduled.scheduler.TestScheduler2   : schedule2, START, current thread: my-test-thread-2
2022-11-16 12:28:32.005  INFO 26169 --- [y-test-thread-2] c.b.scheduled.scheduler.TestScheduler2   : schedule2, END, current thread: my-test-thread-2
2022-11-16 12:28:33.003  INFO 26169 --- [y-test-thread-2] c.b.scheduled.scheduler.TestScheduler2   : schedule2, START, current thread: my-test-thread-2
2022-11-16 12:28:33.004  INFO 26169 --- [y-test-thread-2] c.b.scheduled.scheduler.TestScheduler2   : schedule2, END, current thread: my-test-thread-2
2022-11-16 12:28:33.006  INFO 26169 --- [y-test-thread-1] c.b.scheduled.scheduler.TestScheduler2   : schedule1, END, current thread: my-test-thread-1
```

예상했던 대로 쓰레드가 2개이기 때문에 my-test-thread-1에서 schedule1 작업을 하고 있는 동안 my-test-thread-2에서 열심히 schedule2 작업을 하고 있는 것을 볼 수 있다.
scheduler2 작업 시작 시간이 schedule1 작업 처리 시간에 영향을 받지 않게 된 것이다. 

하지만 scheduler1작업은 1초마다 실행되도록 해놓았지만, 위의 로그에서는 여전히 내부의 로직 처리 시간에 구애받고 있는 것을 알 수 있다. 엄밀히 말해서 개발자가 정해놓은 시간마다 작동하지 않는 것이다.

### 멀티 쓰레드, 비동기 처리 

여전히 스케줄링 작업이 동기적으로 처리되고 있음을 알 수 있다. 그렇다면 원래 의도대로 정해진 시간에 주기적인 작업을 해주기 위해 비동기적 처리를 해보자.

```java
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class ScheduledApplication {
    ...
}
```

비동기 작업을 위해 `@EnableAsync` 을 선언한다.

```java
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
```

비동기 작업을 원하는 스케줄링 메서드에 `@Async` 을 붙여주면? 

```logcatfilter
2022-11-16 12:54:46.168  INFO 26422 --- [           main] c.b.scheduled.ScheduledApplication       : Started ScheduledApplication in 1.678 seconds (JVM running for 2.253)
2022-11-16 12:54:47.015  INFO 26422 --- [         task-1] c.b.scheduled.scheduler.TestScheduler3   : schedule1, START, current thread: task-1
2022-11-16 12:54:48.003  INFO 26422 --- [         task-2] c.b.scheduled.scheduler.TestScheduler3   : schedule1, START, current thread: task-2
2022-11-16 12:54:49.005  INFO 26422 --- [         task-3] c.b.scheduled.scheduler.TestScheduler3   : schedule1, START, current thread: task-3
2022-11-16 12:54:50.003  INFO 26422 --- [         task-4] c.b.scheduled.scheduler.TestScheduler3   : schedule1, START, current thread: task-4
2022-11-16 12:54:51.001  INFO 26422 --- [         task-5] c.b.scheduled.scheduler.TestScheduler3   : schedule1, START, current thread: task-5
2022-11-16 12:54:52.003  INFO 26422 --- [         task-6] c.b.scheduled.scheduler.TestScheduler3   : schedule1, START, current thread: task-6
2022-11-16 12:54:52.019  INFO 26422 --- [         task-1] c.b.scheduled.scheduler.TestScheduler3   : schedule1, END, current thread: task-1
2022-11-16 12:54:53.003  INFO 26422 --- [         task-7] c.b.scheduled.scheduler.TestScheduler3   : schedule1, START, current thread: task-7
2022-11-16 12:54:53.004  INFO 26422 --- [         task-2] c.b.scheduled.scheduler.TestScheduler3   : schedule1, END, current thread: task-2
2022-11-16 12:54:54.002  INFO 26422 --- [         task-8] c.b.scheduled.scheduler.TestScheduler3   : schedule1, START, current thread: task-8
2022-11-16 12:54:54.010  INFO 26422 --- [         task-3] c.b.scheduled.scheduler.TestScheduler3   : schedule1, END, current thread: task-3
2022-11-16 12:54:55.002  INFO 26422 --- [         task-1] c.b.scheduled.scheduler.TestScheduler3   : schedule1, START, current thread: task-1
2022-11-16 12:54:55.004  INFO 26422 --- [         task-4] c.b.scheduled.scheduler.TestScheduler3   : schedule1, END, current thread: task-4
2022-11-16 12:54:56.004  INFO 26422 --- [         task-2] c.b.scheduled.scheduler.TestScheduler3   : schedule1, START, current thread: task-2
2022-11-16 12:54:56.005  INFO 26422 --- [         task-5] c.b.scheduled.scheduler.TestScheduler3   : schedule1, END, current thread: task-5
2022-11-16 12:54:57.003  INFO 26422 --- [         task-3] c.b.scheduled.scheduler.TestScheduler3   : schedule1, START, current thread: task-3
2022-11-16 12:54:57.006  INFO 26422 --- [         task-6] c.b.scheduled.scheduler.TestScheduler3   : schedule1, END, current thread: task-6
2022-11-16 12:54:58.004  INFO 26422 --- [         task-4] c.b.scheduled.scheduler.TestScheduler3   : schedule1, START, current thread: task-4
2022-11-16 12:54:58.004  INFO 26422 --- [         task-7] c.b.scheduled.scheduler.TestScheduler3   : schedule1, END, current thread: task-7
2022-11-16 12:54:59.004  INFO 26422 --- [         task-5] c.b.scheduled.scheduler.TestScheduler3   : schedule1, START, current thread: task-5
2022-11-16 12:54:59.004  INFO 26422 --- [         task-8] c.b.scheduled.scheduler.TestScheduler3   : schedule1, END, current thread: task-8
```

schedule1 작업이 의도했던 주기대로 처리되고 있음을 알 수 있다.

## 마무리


추가적으로 해당 스케줄링 작업이 있는 애플리케이션 서버를 이중화할 때도 고려해봐야 한다. 프로젝트 당시에는 스케줄링 작업을 해당 프로젝트 서비스의 백엔드 서버에 추가했고, 해당 서버를 이중화해야 하는 상황이었다. 백엔드 서버를 이중화하게 되면 각 서버마다 똑같은 스케줄링 작업이 중복으로 돌아가게 되는 문제가 발생하여, @Profile을 사용하여 원하는 서버 하나에만 스케줄링 작업이 동작하도록 처리해줬다.

이번에 @Scheduled에 대해 공부하고, 테스트를 진행하면서 내가 사용하려던 의도에 맞게 @Scheduled를 사용할 수 있게 되었다고 생각한다. 혹시나 간단하게 스케줄링 작업이 필요해 @Scheduled를 사용하고자 하는 분들도 이 글을 보고 도움을 받으셨으면 좋겠다.


## References

https://pompitzz.github.io/blog/Spring/Scheduler.html#_1-%E1%84%89%E1%85%B3%E1%84%8F%E1%85%A6%E1%84%8C%E1%85%AE%E1%86%AF%E1%84%85%E1%85%B5%E1%86%BC-%E1%84%92%E1%85%AA%E1%86%AF%E1%84%89%E1%85%A5%E1%86%BC%E1%84%92%E1%85%AA-%E1%84%86%E1%85%B5%E1%86%BE-%E1%84%89%E1%85%B5%E1%86%AF%E1%84%92%E1%85%A2%E1%86%BC
