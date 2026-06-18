package com.zrws.approval.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理
 */
@ControllerAdvice(basePackages = "com.zrws.approval.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", -1);
        body.put("msg", "服务异常: " + e.getMessage());
        return ResponseEntity.ok(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", -2);
        body.put("msg", e.getMessage());
        return ResponseEntity.ok(body);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", -3);
        body.put("msg", e.getMessage());
        return ResponseEntity.ok(body);
    }
}
