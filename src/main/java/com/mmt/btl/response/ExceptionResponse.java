package com.mmt.btl.response;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ExceptionResponse {
    private String message;
    private HttpStatus status;
    private StackTraceElement[] stackTraceElements;
}