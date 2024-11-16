package com.mmt.btl.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.mmt.btl.response.ExceptionResponse;

@ControllerAdvice
public class HandleExceptionCustom {
    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<?> throwLoginFailedException(LoginFailedException ex) {
        ExceptionResponse response = new ExceptionResponse();
        response.setMessage(ex.getMessage());
        response.setStatus(HttpStatus.BAD_REQUEST);
        response.setStackTraceElements(ex.getStackTrace());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MMTNotFoundException.class)
    public ResponseEntity<?> throwNotFoundException(MMTNotFoundException ex) {
        ExceptionResponse response = new ExceptionResponse();
        response.setMessage(ex.getMessage());
        response.setStatus(HttpStatus.NOT_FOUND);
        response.setStackTraceElements(ex.getStackTrace());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}