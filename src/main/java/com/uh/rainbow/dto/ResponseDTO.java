package com.uh.rainbow.dto;

import org.springframework.http.HttpStatus;

import java.util.Date;

/**
 * <b>File:</b> ResponseDTO.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class ResponseDTO{
    public Date timestamp;
    public ResponseDTO(){
       this.timestamp = new Date();
    }
}
