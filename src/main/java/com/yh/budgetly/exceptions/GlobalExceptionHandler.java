package com.yh.budgetly.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleException(CustomException exception, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        errorResponse.setStatus(exception.getStatus().value());
        errorResponse.setError(exception.getError());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
        errorResponse.setDescription(exception.getMessage());

        log.error("Handling CustomException: {}", errorResponse);

        return new ResponseEntity<>(errorResponse, exception.getStatus());
    }


}
