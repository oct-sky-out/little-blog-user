package com.octskyout.users.token;

public record JwtDecoded(Header header, Payload payload){
    public record Header(String type, String algorithm) { }
    public record Payload (String issuer, String githubProfile, Long expireTime, Long issueTime,
                    String email, String username) { }
}




