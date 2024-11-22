package com.mmt.btl.service;

import javax.servlet.http.HttpServletRequest;

import com.mmt.btl.exception.LoginFailedException;
import com.mmt.btl.request.LoginRequest;
import com.mmt.btl.request.RegisterRequest;
import com.mmt.btl.response.LoginResponse;

public interface UserService {
    public LoginResponse login(HttpServletRequest servletRequest, LoginRequest request) throws LoginFailedException;

    public void register(RegisterRequest request) throws LoginFailedException;
}