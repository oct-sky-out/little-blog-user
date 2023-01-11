package com.octskyout.users.oauth.schedule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;

@Slf4j
public class AccountCronRemote {
    public void assertSchedule(String cronExpression, LocalDateTime initialTime,
                                List<LocalDateTime> expectedLocalDateTimes) {
        CronTrigger trigger = new CronTrigger(cronExpression);
        Date startTime;
        startTime = Timestamp.valueOf(initialTime);

        SimpleTriggerContext context = new SimpleTriggerContext();
        context.update(startTime, startTime, startTime);

        expectedLocalDateTimes.forEach(expectTime -> {
            Date nextExecutionTime = trigger.nextExecutionTime(context);
            LocalDateTime actualTime =
                new Timestamp(nextExecutionTime.getTime()).toLocalDateTime();

            log.info("---------------------------------------");
            log.info("스케쥴링 기대 작동시간 : {}", expectTime);
            log.info("스케쥴링 실제 작동시간 : {}", actualTime);
            log.info("---------------------------------------");
            assertThat("executed on expected time", actualTime, is(expectTime));
            context.update(nextExecutionTime, nextExecutionTime, nextExecutionTime);
        });
    }

    public AccountCronRemoteInfo getCornExpressionFromMethod(Class<?> clazz, String methodName) {
        Method scheduleMethod;
        try {
            scheduleMethod = clazz.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            log.error("메서드가 존재하지않음.");
            throw new RuntimeException(e);
        }
        Scheduled scheduleInfo = scheduleMethod.getAnnotation(Scheduled.class);
        String cronExpression = scheduleInfo.cron();
        return new AccountCronRemoteInfo(this, cronExpression);
    }
}

record AccountCronRemoteInfo(AccountCronRemote remote, String cornPattern) {
}
