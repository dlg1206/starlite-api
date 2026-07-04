package com.uh.rainbow.handler;

import com.uh.rainbow.exception.*;
import com.uh.rainbow.response.InvalidRequestBodyResponse;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <b>File:</b> GlobalExceptionHandler.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Log error using the logger of the throwing class if available, this logger by default
     *
     * @param e Exception thrown
     */
    private void logError(Exception e) {
        Throwable root = e.getCause() != null ? e.getCause() : e;
        StackTraceElement[] stackTrace = root.getStackTrace();
        String throwingClass = (stackTrace.length > 0)
                ? stackTrace[0].getClassName()
                : null;
        // get throwing class, default to this
        if (throwingClass == null || throwingClass.isEmpty()) {
            LoggerFactory.getLogger(GlobalExceptionHandler.class).error(e.getMessage());
        } else {
            LoggerFactory.getLogger(throwingClass).error(e.getMessage());
        }
    }

    /**
     * Handle invalid request params
     *
     * @param e Details on why validation failed
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<InvalidRequestBodyResponse> handleValidation(MethodArgumentNotValidException e) {
        logError(e);
        return ResponseEntity.badRequest().body(new InvalidRequestBodyResponse(e));
    }

    /**
     * Handle invalid request params
     *
     * @param e Details on why pasring failed
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<InvalidRequestBodyResponse> handleValidation(HttpMessageNotReadableException e) {
        logError(e);
        return ResponseEntity.badRequest().body(new InvalidRequestBodyResponse(e));
    }

    /**
     * Handle invalid campus
     *
     * @param e {@link InvalidCampusCodeException}
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(InvalidCampusCodeException.class)
    public ResponseEntity<InvalidCampusCodeException.Response> handleInvalidCampus(InvalidCampusCodeException e) {
        logError(e);
        return ResponseEntity.badRequest().body(e.toResponse());
    }

    /**
     * Handle invalid term codes
     *
     * @param e {@link InvalidTermCodeException}
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(InvalidTermCodeException.class)
    public ResponseEntity<InvalidTermCodeException.Response> handleInvalidCampusTerm(InvalidTermCodeException e) {
        logError(e);
        return ResponseEntity.badRequest().body(e.toResponse());
    }

    /**
     * Handle invalid subject codes
     *
     * @param e {@link InvalidSubjectCodesException}
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(InvalidSubjectCodesException.class)
    public ResponseEntity<InvalidSubjectCodesException.Response> handleInvalidCampusTermSubjects(InvalidSubjectCodesException e) {
        logError(e);
        return ResponseEntity.badRequest().body(e.toResponse());
    }

    /**
     * Handle invalid course IDs
     *
     * @param e {@link InvalidCourseIDsException}
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(InvalidCourseIDsException.class)
    public ResponseEntity<InvalidCourseIDsException.Response> handleInvalidCourseIDs(InvalidCourseIDsException e) {
        logError(e);
        return ResponseEntity.badRequest().body(e.toResponse());
    }


    /**
     * Handle invalid course reference numbers
     *
     * @param e {@link InvalidCourseReferenceNumberException}
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(InvalidCourseReferenceNumberException.class)
    public ResponseEntity<InvalidCourseReferenceNumberException.Response> handleInvalidCRNs(InvalidCourseReferenceNumberException e) {
        logError(e);
        return ResponseEntity.badRequest().body(e.toResponse());
    }

    /**
     * Handle invalid time spans
     *
     * @param e {@link InvalidTimeSpansException}
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(InvalidTimeSpansException.class)
    public ResponseEntity<InvalidTimeSpansException.Response> handleInvalidTimeSpans(InvalidTimeSpansException e) {
        logError(e);
        return ResponseEntity.badRequest().body(e.toResponse());
    }
}
