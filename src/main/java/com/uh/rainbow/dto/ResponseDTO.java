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
    private final static String UH_ROOT = "https://www.sis.hawaii.edu/uhdad/avail.classes";

    private final Date timestamp = new Date();
    private final String source;

    public ResponseDTO(){
        this.source = UH_ROOT;
    }
    public ResponseDTO(String instID){
       this.source = String.format("%s?i=%s", UH_ROOT, instID);
    }

    public ResponseDTO(String instID, String termID){
        this.source = String.format("%s?i=%s&t=%s", UH_ROOT, instID, termID);
    }

    public ResponseDTO(String instID, String termID, String subjectID){
        this.source = String.format("%s?i=%s&t=%s&s=%s", UH_ROOT, instID, termID, subjectID);
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public String getSource(){
        return this.source;
    }
}
