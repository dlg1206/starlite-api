package com.uh.rainbow.util.logging;

import org.jsoup.HttpStatusException;
import org.slf4j.LoggerFactory;

/**
 * <b>File:</b> Logger.java
 * <p>
 * <b>Description:</b> Wrapper for slf4j.Logger to use standardized messages
 *
 * @author Derek Garcia
 */
public class Logger {

    private final org.slf4j.Logger log;

    /**
     * Create new logger for class
     *
     * @param reference Class reference
     */
    public Logger(Class reference) {
        this.log = LoggerFactory.getLogger(reference);
    }


    /**
     * Util logging method for reporting HTTP failures
     *
     * @param type Log type
     * @param e    HttpStatusException
     */
    public void reportHTTPAccessError(MessageBuilder.Type type, HttpStatusException e) {
        MessageBuilder mb = new MessageBuilder(type)
                .addDetails("Failed to fetch HTML")
                .addDetails(e.getStatusCode());
        warn(mb);
        debug(mb.addDetails(e));
    }

    /**
     * Log a debug message
     *
     * @param mb MessageBuilder loaded with message details
     */
    public void debug(MessageBuilder mb) {
        this.log.debug(mb.build());
    }

    /**
     * Log an info message
     *
     * @param mb MessageBuilder loaded with message details
     */
    public void info(MessageBuilder mb) {
        this.log.info(mb.build());
    }

    /**
     * Log a warn message
     *
     * @param mb MessageBuilder loaded with message details
     */
    public void warn(MessageBuilder mb) {
        this.log.warn(mb.build());
    }


    /**
     * Log an error message
     *
     * @param mb MessageBuilder loaded with message details
     */
    public void error(MessageBuilder mb) {
        this.log.error(mb.build());
    }


}
