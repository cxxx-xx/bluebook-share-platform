package com.bluebook.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", e.getMessage());
        
        if (e.getMessage().equals("未登录")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
}
