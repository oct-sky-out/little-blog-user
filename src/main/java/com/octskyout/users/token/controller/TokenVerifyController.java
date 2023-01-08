package com.octskyout.users.token.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.octskyout.users.token.JWTUtil;
import com.octskyout.users.token.JwtDecoded;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class TokenVerifyController {
    private final JWTUtil jwtUtil;

    @GetMapping(value = "/verify", headers = HttpHeaders.AUTHORIZATION)
    public EntityModel<Map<String,JwtDecoded>> verifyJwtToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String jwtBearerToken) {
        String jwtToken = getJwtToken(jwtBearerToken);
        JwtDecoded decodedJWT = jwtUtil.verifyToken(jwtToken);

        EntityModel<Map<String,JwtDecoded>> selfRel =
            methodOn(TokenVerifyController.class).verifyJwtToken(jwtBearerToken);

        return EntityModel.of(Map.of("token", decodedJWT),
            linkTo(selfRel).withSelfRel());
    }

    private String getJwtToken(String jwtBearerToken) {
        return jwtBearerToken.trim().split(" ")[1];
    }
}
