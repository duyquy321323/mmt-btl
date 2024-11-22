package com.mmt.btl.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String username;
    private Long id;
    private Long expiryTime;
    private String token;
}