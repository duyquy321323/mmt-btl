package com.mmt.btl.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mmt.btl.request.LoginRequest;
import com.mmt.btl.request.RegisterRequest;
import com.mmt.btl.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    final private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest servletRequest, @RequestBody LoginRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.login(servletRequest, request));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}