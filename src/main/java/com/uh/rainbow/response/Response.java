package com.uh.rainbow.response;

import java.util.Date;

/**
 * <b>File:</b> ResponseDTO.java
 * <p>
 * <b>Description:</b> Generic base response class for all API requests
 *
 * @author Derek Garcia
 */
public abstract class Response {
    // DO NOT REMOVE - included when returned
    public Date timestamp = new Date();
}
