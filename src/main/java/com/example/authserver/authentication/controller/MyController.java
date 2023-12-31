package com.example.authserver.authentication.controller;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class MyController {

    @GetMapping("/admin")
    public String homeAdmin(Principal principal) {

        return "Hello mr. " + principal.getName();
    }
    @GetMapping("/user")
    public String homeUser(Principal principal) {
        var ds=((JwtAuthenticationToken)principal).getCredentials();
        var result= ((Jwt)ds).getClaims().get("test_id");
        System.out.println(result);
        return "Hello mr. " + principal.getName();
    }
}