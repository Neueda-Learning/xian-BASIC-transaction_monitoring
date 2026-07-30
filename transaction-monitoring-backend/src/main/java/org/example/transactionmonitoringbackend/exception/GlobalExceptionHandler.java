package org.example.transactionmonitoringbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Handles 404 errors when an alert or transaction cannot be found by the given id. */
    @ExceptionHandler({AlertNotFoundException.class, TransactionNotFoundException.class})
    public ResponseEntity<ApiError> handleNotFound(RuntimeException e) {
        ApiError error = new ApiError();
        error.setStatus(404);
        error.setError("Not Found");
        error.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /** Handles 400 errors when an alert status transition is not permitted by business rules. */
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ApiError> handleInvalidStatus(InvalidStatusTransitionException e) {
        ApiError error = new ApiError();
        error.setStatus(400);
        error.setError("Bad Request");
        error.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /** Handles 400 errors when request data fails validation checks. */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidation(ValidationException e) {
        ApiError error = new ApiError();
        error.setStatus(400);
        error.setError("Bad Request");
        error.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /** Catch-all handler for any unhandled exception, returning a 500 Internal Server Error. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception e) {
        ApiError error = new ApiError();
        error.setStatus(500);
        error.setError("Internal Server Error");
        error.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
