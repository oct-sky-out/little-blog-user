package com.octskyout.users.oauth.repository;

import com.octskyout.users.oauth.entity.OauthUser;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OauthUserRepository extends JpaRepository<OauthUser, String> {
    Optional<OauthUser> findOauthUserByUsername(String username);

    @Query("SELECT O FROM OauthUser as O WHERE O.lastLoginDateTime < :oneYearAgoDateTime")
    Page<OauthUser> findLastLoginAtOneYearsAgoUsers(@Param("oneYearAgoDateTime") LocalDateTime oneYearAgoDateTime,
                                                    Pageable pageable);
}
