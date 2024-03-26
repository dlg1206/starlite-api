package com.uh.rainbow.entities.timeblock.simple;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * <b>File:</b> SimpleTime.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class SimpleTime extends Simple {

    private final static String TIME_INPUT_FORMAT = "HHmm";
    private final static DateFormat OUTPUT_FORMATTER = new SimpleDateFormat("hh:mm a");

    public SimpleTime(String time) throws ParseException {
        super(TIME_INPUT_FORMAT, time);      // 1/1/70 HH:mm
    }

    @Override
    public String toString() {
        if(this.timeReference == null)
            return TBA_STRING;

        return OUTPUT_FORMATTER.format(this.timeReference);
    }
}
