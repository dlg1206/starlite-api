package com.uh.rainbow.dto;

import java.util.Date;

/**
 * <b>File:</b> ResponseDTO.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class ResponseDTO{
    private final Date timestamp = new Date();

    public Date getTimestamp() {
        return this.timestamp;
    }
}
