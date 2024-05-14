package com.uh.rainbow.util.logging;

import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>File:</b> MessageBuilder.java
 * <p>
 * <b>Description:</b> Message builder utility for creating consistent logging messages
 *
 * @author Derek Garcia
 */
public class MessageBuilder {
    /**
     * Logging enum type
     */
    public enum Type {
        INST,
        TERM,
        SUBJECT,
        COURSE,
        SCHEDULE
    }

    private static final String DELIMITER = " | ";
    private final Type messageType;
    private final List<Object[]> details = new ArrayList<>();
    private String duration = null;

    /**
     * Create new builder
     *
     * @param messageType Type of log message
     */
    public MessageBuilder(Type messageType) {
        this.messageType = messageType;
    }

    /**
     * Add details to message
     *
     * @param details List of objects to add as strings
     * @return this
     */
    public MessageBuilder addDetails(Object... details) {
        this.details.add(details);
        return this;
    }

    /**
     * Save duration to report elapsed time
     *
     * @param start Start time
     * @return this
     */
    public MessageBuilder setDuration(Instant start) {
        this.duration = "%ss".formatted((double) Duration.between(start, Instant.now()).toMillis() / (double) 1000);
        return this;
    }

    /**
     * Used by logger, generate String log message
     *
     * @return String message
     */
    String build() {
        StringBuilder sb = new StringBuilder().append(this.messageType);

        // Add details if any
        if (!this.details.isEmpty())
            sb.append(DELIMITER);

        for (Object[] details : this.details) {
            sb.append(StringUtils.join(details, " "));
            if (this.details.indexOf(details) != this.details.size() - 1)
                sb.append(DELIMITER);
        }

        // Add elapsed time if any
        if (this.duration != null)
            sb.append(DELIMITER).append("Completed in ").append(this.duration);

        return sb.toString();
    }

}