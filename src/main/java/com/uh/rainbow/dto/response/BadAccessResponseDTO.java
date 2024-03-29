package com.uh.rainbow.dto.response;

import com.uh.rainbow.util.SourceURL;
import org.jsoup.HttpStatusException;

/**
 * <b>File:</b> BadAccessResponseDTO.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class BadAccessResponseDTO extends ResponseDTO{
    public final String source;
    public final int response_code;
    public final String response_message;
    public final String error_msg = "Failed to access resource at source url; Check to make sure the source url is valid";

    public BadAccessResponseDTO(HttpStatusException e){
        this.source = e.getUrl();
        this.response_code = e.getStatusCode();
        this.response_message = e.getMessage();
    }
}
