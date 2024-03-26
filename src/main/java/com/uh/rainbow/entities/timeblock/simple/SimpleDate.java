package com.uh.rainbow.entities.timeblock.simple;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * <b>File:</b> SimpleDate.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class SimpleDate extends Simple {

    private final static String DATE_INPUT_FORMAT = "MM/dd";
    private final static DateFormat OUTPUT_FORMATTER = new SimpleDateFormat("MM/dd");

    public SimpleDate(String date) throws ParseException {
        super(DATE_INPUT_FORMAT, date);     // dd/MM/70 00:00
    }

    @Override
    public String toString() {

        if(this.timeReference == null)
            return TBA_STRING;

        return OUTPUT_FORMATTER.format(this.timeReference);
    }
}
