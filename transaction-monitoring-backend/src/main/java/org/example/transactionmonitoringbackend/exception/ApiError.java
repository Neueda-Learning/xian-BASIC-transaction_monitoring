package org.example.transactionmonitoringbackend.exception;

import java.time.LocalDateTime;

/** Represents the structured error body returned in HTTP error responses. */
public class ApiError {
    private LocalDateTime timestamp; // time the error occurred
    private int status;              // HTTP status code
    private String error;            // short error reason
    private String message;          // detailed description of the error
    private String path;             // request URI that triggered the error

    public ApiError() {
    }

    /** All-args constructor for building a complete error response in one call. */
    public ApiError(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}