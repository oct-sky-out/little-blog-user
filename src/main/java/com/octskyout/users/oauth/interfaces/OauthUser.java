package com.octskyout.users.oauth.interfaces;

public interface OauthUser<T> {
    String getUsername();

    T getId();

    String getEmail();
}
