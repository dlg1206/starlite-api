package com.uh.rainbow.util.logging;

import com.uh.rainbow.controller.RainbowController;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * <b>File:</b> Logger.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class Logger {

    private final org.slf4j.Logger log;
    public Logger(Class reference){
        this.log = LoggerFactory.getLogger(reference);
    }


    public void logEndpointAccess(String... path){
        String fullEndpoint = StringUtils.join(path);
        this.log.info("Request to endpoint: " + fullEndpoint);
    }

    public void warn(String msg){
        this.log.warn("Reason: " + msg);
    }

    public void error(String msg){
        this.log.error("Reason: " + msg);
    }

    public DurationLogger createDurationLogger(){
        return new DurationLogger(this.log);
    }
}
