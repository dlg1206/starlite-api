package com.uh.rainbow.response;

/**
 * <b>File:</b> APIErrorResponseDTO.java
 * <p>
 * <b>Description:</b> Generic error with rainbow iteself
 *
 * @author Derek Garcia
 */
public class RainbowErrorResponse extends Response {
    // DO NOT REMOVE - included when returned
    public final String error_message = "Something failed when processing request";
    public final String error;

    public RainbowErrorResponse(Exception e) {
        this.error = e.getMessage();
    }
}
