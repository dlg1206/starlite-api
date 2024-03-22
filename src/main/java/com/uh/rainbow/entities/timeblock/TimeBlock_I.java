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

    private final static Date SIX_AM;

    static {
        try {
            SIX_AM = INPUT.parse("0600");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    protected String startTime;
    protected String startDate;
    protected String endTime;
    protected String endDate;

    /**
     * Default timeblock constructor
     *
     * @param startTime Start time of class
     * @param startDate Start Date of class
     * @param endTime   End time of class
     * @param endDate   End Date of class
     */
    protected TimeBlock_I(String startTime, String startDate, String endTime, String endDate) {
        this.startTime = startTime;
        this.startDate = startDate;
        this.endTime = endTime;
        this.endDate = endDate;
    }

    /**
     * @return Start time
     */
    public String getStartTime() {
        return this.startTime;
    }

    /**
     * @return Start Date
     */
    public String getStartDate() {
        return this.startDate;
    }

    /**
     * @return End time
     */
    public String getEndTime() {
        return this.endTime;
    }

    /**
     * @return End Date
     */
    public String getEndDate() {
        return this.endDate;
    }

    /**
     * Constructor that parses the UH formatted time string into a time block
     *
     * @param timeString Time string in the form: HHMM-HHMM(?:a|p) ie 0900-1200p
     * @return Timeblock with interpreted start and end times
     * @throws ParseException Failed to parse time
     */
    public static TimeBlock_I createTimeBlock(String timeString, String dateString) throws ParseException {

        String[] dates = dateString.split("-");

        // Return special "TBA" block if no time is given
        if (timeString.equalsIgnoreCase("TBA"))
            return new TBATimeBlock(dates[0], dates.length == 2 ? dates[1] : dates[0]);     // Class on 1 day, not range

        // Get a/p and separate start and end times
        char tod = timeString.charAt(timeString.length() - 1);
        String[] times = timeString.substring(0, timeString.length() - 1).split("-");

        Date start = INPUT.parse(times[0]);
        Date end = INPUT.parse(times[1]);

        /*
        Mark start in afternoon if 'p' and before 7
        Handles edge case "0100-0200p", class doesn't start at 1 am but "0900-0200p" class probably starts at 9
        6 AM arbitrary start that assuming no classes occur before
         */
        if (tod == 'p' && start.before(SIX_AM))
            start = DateUtils.addHours(start, 12);

        // Mark end in afternoon if 'p'
        if (tod == 'p' && end.before(NOON))
            end = DateUtils.addHours(end, 12);

        return new TimeBlock(start, dates[0], end, dates.length == 2 ? dates[1] : dates[0]);
    }
}
