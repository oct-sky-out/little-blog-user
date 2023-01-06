package com.octskyout.users.token;

public enum TokenType {
    ACCESS(900000L),
    REFRESH(259200000L);

    private final long expMilliSeconds;

    TokenType(long expMilliSeconds) {
        this.expMilliSeconds = expMilliSeconds;
    }

    public long getExpMilliSeconds() {
        return expMilliSeconds;
    }
}
