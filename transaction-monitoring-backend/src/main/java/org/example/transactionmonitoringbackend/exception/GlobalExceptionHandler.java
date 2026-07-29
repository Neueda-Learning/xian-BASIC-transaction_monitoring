package org.example.transactionmonitoringbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException e) {
        ApiError error = new ApiError();
        error.setStatus(404);
        error.setError("Not Found");
        error.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ApiError> handleInvalidStatus(InvalidStatusTransitionException e) {
        ApiError error = new ApiError();
        error.setStatus(400);
        error.setError("Bad Request");
        error.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception e) {
        ApiError error = new ApiError();
        error.setStatus(500);
        error.setError("Internal Server Error");
        error.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
