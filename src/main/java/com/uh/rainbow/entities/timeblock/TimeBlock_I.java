package com.uh.rainbow.entities.timeblock;

import org.apache.commons.lang3.time.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <b>File:</b> TimeBlock_I.java
 * <p>
 * <b>Description:</b> Generic Time Block to record the start and end of a class
 *
 * @author Derek Garcia
 */
public abstract class TimeBlock_I {
    private final static DateFormat INPUT = new SimpleDateFormat("HHmm");
    // used for "afternoon" checks
    private final static Date NOON;

    static {
        try {
            NOON = INPUT.parse("1200");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    protected String start;
    protected String end;

    /**
     * Default timeblock constructor
     *
     * @param start Start time of class
     * @param end End time of class
     */
    protected TimeBlock_I(String start, String end) {
        this.start = start;
        this.end = end;
    }

    /**
     * @return Start time
     */
    public String getStart() {
        return this.start;
    }

    /**
     * @return End time
     */
    public String getEnd() {
        return this.end;
    }

    /**
     * Constructor that parses the UH formatted time string into a time block
     *
     * @param timeString Time string in the form: HHMM-HHMM(?:a|p) ie 0900-1200p
     * @return Timeblock with interpreted start and end times
     * @throws ParseException Failed to parse time
     */
    public static TimeBlock_I createTimeBlock(String timeString) throws ParseException {
        // Return special "TBA" block if no time is given
        if (timeString.equalsIgnoreCase("TBA"))
            return new TBATimeBlock();

        // Get a/p and separate start and end times
        char tod = timeString.charAt(timeString.length() - 1);
        String[] times = timeString.substring(0, timeString.length() - 1).split("-");

        Date start = INPUT.parse(times[0]);
        Date end = INPUT.parse(times[1]);

        // Mark end in afternoon if 'p'
        if (tod == 'p' && end.before(NOON))
            end = DateUtils.addHours(end, 12);

        return new TimeBlock(start, end);
    }
}
