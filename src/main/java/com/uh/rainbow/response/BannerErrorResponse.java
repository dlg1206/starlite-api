package com.uh.rainbow.response;

import org.springframework.web.client.HttpStatusCodeException;

/**
 * <b>File:</b> BadAccessResponseDTO.java
 * <p>
 * <b>Description:</b> Report failure to access Banner9 API
 *
 * @author Derek Garcia
 */
public class BannerErrorResponse extends Response {
    public final String source;
    public final int response_code;
    public final String response_message;

    /**
     * Create new failure
     *
     * @param url URL of API
     * @param e   Error message
     */
    public BannerErrorResponse(String url, HttpStatusCodeException e) {
        this.source = url;
        this.response_code = e.getStatusCode().value();
        this.response_message = e.getMessage();
    }
}
