package com.uh.rainbow.dto.response;

/**
 * <b>File:</b> APIErrorResponseDTO.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class APIErrorResponseDTO extends ResponseDTO {
    public final String error_message = "Something failed when processing request";
    public final String error;

    public APIErrorResponseDTO(Exception e) {
        this.error = e.getMessage();
    }
}
