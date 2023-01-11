package com.octskyout.users.oauth.schedule;

import com.octskyout.users.oauth.entity.OauthUser;
import com.octskyout.users.oauth.repository.OauthUserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableAsync
@RequiredArgsConstructor
@Slf4j
public class AccountDeleteSchedule {
    private final OauthUserRepository oauthUserRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Async
    public void finalLoggedAfterOneYearAccountDelete() {
        LocalDateTime oneYearsAgo = LocalDate.now()
            .minusYears(1L)
            .atTime(0, 0);

        Long threadNumber = Thread.currentThread().getId();
        log.debug("thread num : {}", threadNumber);
        Pageable pageable = PageRequest.of(threadNumber.intValue(),30);

        Page<OauthUser> deleteTargets =
            oauthUserRepository.findLastLoginAtOneYearsAgoUsers(oneYearsAgo, pageable);

        oauthUserRepository.deleteAll(deleteTargets.getContent());
    }
}
