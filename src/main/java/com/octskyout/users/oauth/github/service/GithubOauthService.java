package com.octskyout.users.oauth.github.service;

import com.octskyout.users.oauth.entity.OauthUser;
import com.octskyout.users.oauth.github.dto.GithubUserDto;
import com.octskyout.users.oauth.repository.OauthUserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GithubOauthService {
    public static final String ADMIN_USERNAME = "oct-sky-out";
    private final OauthUserRepository oauthUserRepository;

    @Transactional
    public Boolean doSignIn(GithubUserDto githubUserDto){
        Optional<OauthUser> oauthUser =
            oauthUserRepository.findOauthUserByUsername(githubUserDto.getUsername());

        oauthUser.ifPresentOrElse(this::presentDoSignIn, noPresentDoSignUp(githubUserDto));
        return isAdmin(githubUserDto.getUsername());
    }

    private void presentDoSignIn(OauthUser oauthUser) {
        oauthUser.writeLoinDateTime();
    }

    private Runnable noPresentDoSignUp(GithubUserDto githubUserDto) {
        return () -> oauthUserRepository.save(OauthUser.createGithubOauthUser(githubUserDto));
    }

    private Boolean isAdmin(String username) {
        return username.equals(ADMIN_USERNAME);
    }
}
