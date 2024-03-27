package com.uh.rainbow.entities.timeblock.simple;

import org.apache.commons.lang3.time.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * <b>File:</b> SimpleTime.java
 * <p>
 * <b>Description:</b> Simple representation of a time
 *
 * @author Derek Garcia
 */
public class SimpleTime extends Simple {
    private final static String TIME_INPUT_FORMAT = "HHmm";
    private final static DateFormat OUTPUT_FORMATTER = new SimpleDateFormat("hh:mm a");

    /**
     * Create new Simple Time
     *
     * @param time time in the format of 'HHmm' ( 0900, 0230, etc )
     * @throws ParseException Failed to parse time string
     */
    public SimpleTime(String time) throws ParseException {
        super(TIME_INPUT_FORMAT, time);      // 1/1/70 HH:mm
    }

    /**
     * Add hours to this Simple Time
     *
     * @param numHours Number of hours to add
     */
    public void addHours(int numHours) {
        // Can't add time to null / TBA
        if(this.timeReference == null)
            return;
        this.timeReference = DateUtils.addHours(this.timeReference, numHours);
    }

    @Override
    public String toString() {
        // TBA string
        if(this.timeReference == null)
            return TBA_STRING;

        return OUTPUT_FORMATTER.format(this.timeReference);
    }
}
