package com.uh.rainbow.entities.timeblock.simple;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * <b>File:</b> SimpleTime.java
 * <p>
 * <b>Description:</b> Simple representation of a date
 *
 * @author Derek Garcia
 */
public class SimpleDate extends Simple {
    private final static String DATE_INPUT_FORMAT = "MM/dd";
    private final static DateFormat OUTPUT_FORMATTER = new SimpleDateFormat("MM/dd");


    /**
     * Create new Simple Date
     *
     * @param date date in the format of 'MM/dd' ( 08/22, 12/10, etc )
     * @throws ParseException Failed to parse date string
     */
    public SimpleDate(String date) throws ParseException {
        super(DATE_INPUT_FORMAT, date);     // dd/MM/70 00:00
    }

    @Override
    public String toString() {
        // TBA string
        if(this.timeReference == null)
            return TBA_STRING;

        return OUTPUT_FORMATTER.format(this.timeReference);
    }
}
