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
public record ResponseDTO(Date timestamp, String msg) {
   public ResponseDTO(String msg){
       this(new java.util.Date(), msg);
   }
}
