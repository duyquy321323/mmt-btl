package com.mmt.btl.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LogServerResponse {
    private String message;
    private String username;
    private String userAgent;
}