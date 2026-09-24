package com.alibaba.otter.manager.web.api;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> status(ResponseStatusException error) {
        return ResponseEntity.status(error.getStatusCode())
            .body(Map.of("message", error.getReason() == null ? "请求无法完成" : error.getReason()));
    }

    @ExceptionHandler({IllegalArgumentException.class, tools.jackson.core.JacksonException.class, org.springframework.http.converter.HttpMessageNotReadableException.class})
    public ResponseEntity<?> invalid(Exception error) {
        return ResponseEntity.badRequest().body(Map.of("message", "请检查必填字段和参数格式"));
    }

    @ExceptionHandler(RepeatConfigureException.class)
    public ResponseEntity<?> duplicate(Exception error) {
        return ResponseEntity.status(409).body(Map.of("message", "配置已存在，请检查名称和关联关系"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> failure(Exception error) {
        logger.error("Manager API request failed", error);
        return ResponseEntity.internalServerError().body(Map.of("message", "操作失败，请检查服务状态和运行日志"));
    }
}
