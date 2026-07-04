package com.uh.rainbow.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import tools.jackson.databind.DatabindException;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <b>File:</b> InvalidRequestBodyResponse.java
 * <p>
 * <b>Description:</b> Error for invalid params
 *
 * @author Derek Garcia
 */
@JsonPropertyOrder({"timestamp", "error", "fieldErrors"})
public class InvalidRequestBodyResponse {
    public static final String error = "Validation error";  // do not remove
    public final Date timestamp;
    public final List<FieldErrorDTO> fieldErrors;

    /**
     * Create new error for invalid params
     *
     * @param e Details on why validation failed
     */
    public InvalidRequestBodyResponse(MethodArgumentNotValidException e) {
        this.timestamp = new Date();
        this.fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(this::buildFieldErrorDetail)
                .collect(Collectors.toList());
    }

    /**
     * Create new error for invalid params
     *
     * @param e Details on why validation failed
     */
    public InvalidRequestBodyResponse(HttpMessageNotReadableException e) {
        this.timestamp = new Date();
        Throwable cause = e.getCause();
        if (cause instanceof DatabindException jme && !jme.getPath().isEmpty()) {
            String fieldName = jme.getPath().stream()
                    .map(DatabindException.Reference::getPropertyName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("."));
            this.fieldErrors = List.of(
                    new FieldErrorDTO(fieldName, null, "Datatype mismatch")
            );
        } else {
            this.fieldErrors = List.of(
                    new FieldErrorDTO(null, null, "Datatype mismatch")
            );
        }
    }

    /**
     * Util method for creating field error DTOs
     *
     * @param e {@link FieldError}
     * @return {@link FieldErrorDTO}
     */
    private FieldErrorDTO buildFieldErrorDetail(FieldError e) {
        return new FieldErrorDTO(e.getField(), e.getRejectedValue(), e.getDefaultMessage());
    }

    /**
     * DTO for storing error details
     *
     * @param field         Issue field
     * @param rejectedValue Value rejected
     * @param reason        Reason value rejected
     */
    public record FieldErrorDTO(String field, Object rejectedValue, String reason) {
    }


}
