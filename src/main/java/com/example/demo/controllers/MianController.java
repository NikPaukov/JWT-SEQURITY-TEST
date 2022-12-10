package com.example.demo.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import com.example.demo.repo.UserRepository;
import com.example.demo.security.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@Slf4j
public class MianController {
    UserRepository userRepository;
    UserService userService;

    private class RefreshTokenResponse {
        String refreshToken;
    }

    @GetMapping("/refresh")
    void refreshTokenResponse(HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String refresh_token = authHeader.substring("Bearer ".length());
                Algorithm algorithm = Algorithm.HMAC256("secret".getBytes(StandardCharsets.UTF_8));
                JWTVerifier jwtVerifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = jwtVerifier.verify(refresh_token);
                String username = decodedJWT.getSubject();
                User user = userService.findUserByUsername(username);

                String accessToken = JWT.create().withSubject(user.getUsername()).withExpiresAt(new Date(System.currentTimeMillis() + 3 * 60 * 1000))
                        .withIssuer(request.getRequestURL().toString())
                        .withClaim("authorities", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                        .sign(algorithm);


                Map<String, String> tokens = new HashMap<>();
                tokens.put("accessToken", accessToken);
                tokens.put("refreshToken", refresh_token);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);
            } catch (Exception e) {
                response.setHeader("error", e.getMessage());
                response.setStatus(404);
            }
        }
    }


    @GetMapping("/")
    String get(HttpServletRequest request) {
        log.info(request.getHeaderNames().toString());
        return "Free";

    }

    @GetMapping("/someUser/{name}")
    User getUser(@PathVariable String name) {
        User user = userRepository.findByUsername(name);
        System.out.println(user);
        return user;
    }

    @GetMapping("/secured")
    String getTwo(Principal principal) {
        return "secured";
    }

    @GetMapping("/user")
    String getThree() {
        return "user";
    }

    @GetMapping("/admin")
    String getFour() {
        return "admin";
    }

    @GetMapping("/aadmin")
    String getFive() {
        return "cool admin";
    }
}