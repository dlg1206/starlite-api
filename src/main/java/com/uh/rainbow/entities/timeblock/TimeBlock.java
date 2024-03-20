package com.uh.rainbow.entities.timeblock;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <b>File:</b> TimeBlock.java
 * <p>
 * <b>Description:</b> Time Block with actual start and end times
 *
 * @author Derek Garcia
 */
public class TimeBlock extends TimeBlock_I {
    private final static String TIME_OUTPUT = "hh:mm a";
    private final static DateFormat output = new SimpleDateFormat(TIME_OUTPUT);

    /**
     * Create a new time block using Date times
     *
     * @param start Start time
     * @param end End time
     */
    protected TimeBlock(Date start, Date end) {
        super(output.format(start), output.format(end));
    }

}
