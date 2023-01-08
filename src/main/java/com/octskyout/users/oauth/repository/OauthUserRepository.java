package com.octskyout.users.oauth.repository;

import com.octskyout.users.oauth.entity.OauthUser;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface OauthUserRepository extends CrudRepository<OauthUser, String> {
    Optional<OauthUser> findOauthUserByUsername(String username);
}
