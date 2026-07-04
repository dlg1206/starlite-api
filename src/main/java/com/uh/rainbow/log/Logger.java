package com.uh.rainbow.log;

import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Collection;

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
     * @param e    HttpStatusCodeException
     */
    public void reportBannerAccessError(MessageBuilder.Type type, HttpStatusCodeException e) {
        MessageBuilder mb = new MessageBuilder(type)
                .addDetails("Failed to access Banner9 API")
                .addDetails(e.getStatusCode());
        warn(mb);
        debug(mb.addDetails(e));
    }


    /**
     * Util logging method for reporting missing sections for scheduling
     *
     * @param crns List of missing CRNs
     * @param cids List of missing CIDs
     * @return Error error
     */
    public String reportMissingSchedulingSections(Collection<String> crns, Collection<String> cids) {
        MessageBuilder mb = new MessageBuilder(MessageBuilder.Type.SCHEDULE)
                .addDetails("The following classes have no sections that meet the criteria");
        // Add crn details
        if (crns != null && !crns.isEmpty())
            mb.addDetails("Missing crns: " + String.join(", ", crns));
        // Add cid details
        if (cids != null && !cids.isEmpty())
            mb.addDetails("Missing cids: " + String.join(", ", cids));

        warn(mb);
        return mb.build();
    }

    /**
     * Log a debug error
     *
     * @param mb MessageBuilder loaded with error details
     */
    public void debug(MessageBuilder mb) {
        this.log.debug(mb.build());
    }

    /**
     * Log an info error
     *
     * @param mb MessageBuilder loaded with error details
     */
    public void info(MessageBuilder mb) {
        this.log.info(mb.build());
    }

    /**
     * Log a warn error
     *
     * @param mb MessageBuilder loaded with error details
     */
    public void warn(MessageBuilder mb) {
        this.log.warn(mb.build());
    }


    /**
     * Log an error error
     *
     * @param mb MessageBuilder loaded with error details
     */
    public void error(MessageBuilder mb) {
        this.log.error(mb.build());
    }


}
