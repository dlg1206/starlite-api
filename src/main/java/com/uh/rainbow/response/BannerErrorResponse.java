package com.uh.rainbow.response;

import org.springframework.web.client.HttpStatusCodeException;

import java.util.Date;

/**
 * <b>File:</b> BadAccessResponseDTO.java
 * <p>
 * <b>Description:</b> Report failure to access Banner9 API
 *
 * @author Derek Garcia
 */
public class BannerErrorResponse {
    public final Date timestamp;
    public final String source;
    public final int responseCode;
    public final String response_message;

    /**
     * Create new failure
     *
     * @param url URL of API
     * @param e   Error error
     */
    public BannerErrorResponse(String url, HttpStatusCodeException e) {
        this.timestamp = new Date();
        this.source = url;
        this.responseCode = e.getStatusCode().value();
        this.response_message = e.getMessage();
    }
}
