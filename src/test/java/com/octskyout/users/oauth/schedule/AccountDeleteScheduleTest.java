package com.octskyout.users.oauth.schedule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;

import com.octskyout.users.config.AsyncConfig;
import com.octskyout.users.oauth.dummy.OauthUserDummy;
import com.octskyout.users.oauth.entity.OauthUser;
import com.octskyout.users.oauth.repository.OauthUserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({AccountDeleteSchedule.class, AsyncConfig.class})
@Order(1)
class AccountDeleteScheduleTest {
    @Autowired
    AccountDeleteSchedule schedule;

    @MockBean
    OauthUserRepository oauthUserRepository;

    @Test
    void 회원삭제_스케줄링을_실행하는_로직은_실행되어야한다() {
        OauthUser user = OauthUserDummy.githubUserDummy().get();
        List<OauthUser> deleteUsers = List.of(user);
        Pageable pageable = PageRequest.of(19, 30);
        Page<OauthUser> deleteTargetUsers = new PageImpl<>(deleteUsers, pageable, 1);

        given(oauthUserRepository.findLastLoginAtOneYearsAgoUsers(any(LocalDateTime.class), eq(pageable)))
            .willReturn(deleteTargetUsers);

        willDoNothing()
            .given(oauthUserRepository)
            .deleteAll(deleteUsers);

        schedule.finalLoggedAfterOneYearAccountDelete();

        then(oauthUserRepository)
            .should(times(1))
            .findLastLoginAtOneYearsAgoUsers(any(LocalDateTime.class), any(Pageable.class));
        then(oauthUserRepository)
            .should(times(1))
            .deleteAll(deleteUsers);
    }

    @Test
    void 회원삭제_스케줄링의_cron이_원하는_때에_실행되어야한다() {
        LocalDateTime now = LocalDateTime.of(2023,01,01, 0, 0);
        List<LocalDateTime> expectedLocalDateTimes = List.of(
            now.plusDays(1),
            now.plusDays(2),
            now.plusDays(3)
        );
        AccountCronRemoteInfo remoteInfo = new AccountCronRemote()
            .getCornExpressionFromMethod(AccountDeleteSchedule.class, "finalLoggedAfterOneYearAccountDelete");

        remoteInfo.remote()
            .assertSchedule(remoteInfo.cornPattern(), now, expectedLocalDateTimes);
    }
}
