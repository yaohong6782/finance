package com.rundown.budgetTracking.exceptions;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;


@Slf4j
@Data
public class CustomException extends RuntimeException {
    private final HttpStatus status;
    private final String error;

    public CustomException(String message, HttpStatus status, String error) {
        super(message);
        this.status = status;
        this.error = error;
    }

}
