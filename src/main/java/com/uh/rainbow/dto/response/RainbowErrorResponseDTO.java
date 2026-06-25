package com.uh.rainbow.dto.response;

/**
 * <b>File:</b> APIErrorResponseDTO.java
 * <p>
 * <b>Description:</b> Generic error with rainbow iteself
 *
 * @author Derek Garcia
 */
public class RainbowErrorResponseDTO extends ResponseDTO {
    // DO NOT REMOVE - included when returned
    public final String error_message = "Something failed when processing request";
    public final String error;

    public RainbowErrorResponseDTO(Exception e) {
        this.error = e.getMessage();
    }
}
