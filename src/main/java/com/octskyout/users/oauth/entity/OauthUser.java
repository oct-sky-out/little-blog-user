package com.octskyout.users.oauth.entity;

import com.octskyout.users.oauth.github.dto.GithubUserDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "oauth_user")
@Getter
@NoArgsConstructor
public class OauthUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String username;

    private String email;

    @NotNull
    private LocalDate signUpDate;

    @NotNull
    private LocalDateTime lastLoginDateTime;

    private Long githubId;

    public static OauthUser createGithubOauthUser(GithubUserDto githubUserDto) {
        return new OauthUser(
            githubUserDto.getId(),
            githubUserDto.getUsername(),
            githubUserDto.getEmail());
    }

    private OauthUser(Long githubId, String username, String email) {
        this.githubId = githubId;
        this.username = username;
        this.email = email;
        this.signUpDate = LocalDate.now();
        writeLoinDateTime();
    }


    public void writeLoinDateTime() {
        this.lastLoginDateTime = LocalDateTime.now();
    }
}
