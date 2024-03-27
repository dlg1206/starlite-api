package com.uh.rainbow.entities.timeblock.simple;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * <b>File:</b> Simple.java
 * <p>
 * <b>Description:</b> "Simple" Interface for date and time management
 *
 * @author Derek Garcia
 */
public abstract class Simple extends Date {
    protected static final String TBA_STRING = "TBA";
    protected Date timeReference = null;    // default is null / TBA

    /**
     * Create a new simple "time" interface for date
     *
     * @param formatString SimpleDateFormat string to use
     * @param timeReference "Time Reference", either time or date, to use
     * @throws ParseException Failed to parse timeReference
     */
    protected Simple(String formatString, String timeReference) throws ParseException {
        DateFormat formatter = new SimpleDateFormat(formatString);

        // Parse date if not TBA
        if (!timeReference.equalsIgnoreCase(TBA_STRING))
            this.timeReference = formatter.parse(timeReference);
    }


    /**
     * Calculate duration between this Time Reference and other time reference
     *
     * @param other Other time to compare to
     * @param timeUnit Time Unit to convert into
     * @return -1 if null / TBA, else long value of the given time unit
     */
    public long duration(Simple other, TimeUnit timeUnit){
        // Can't compare TBA dates, so err
        if(this.timeReference == null || other.timeReference == null)
            return -1;
        // Calc diff b/w times
        long diff = Math.abs(this.timeReference.getTime() - other.timeReference.getTime());
        return TimeUnit.valueOf(String.valueOf(timeUnit)).convert(diff, TimeUnit.MILLISECONDS);
    }


    /**
     * Compare this time to another time
     *
     * @param other Other time to compare to
     * @return -1 if null / TBA, 1 if this <= to other, 0 otherwise
     */
    public int beforeOrEqual(Simple other) {
        // Can't compare TBA dates, so err
        if (this.timeReference == null || other.timeReference == null)
            return -1;
        return this.timeReference.getTime() <= other.timeReference.getTime() ? 1 : 0;
    }


    /**
     * Compare this time to another time
     *
     * @param other Other time to compare to
     * @return -1 if null / TBA, 1 if this >= to other, 0 otherwise
     */
    public int afterOrEqual(Simple other) {
        // Can't compare TBA dates, so er
        if (this.timeReference == null || other.timeReference == null)
            return -1;
        return this.timeReference.getTime() >= other.timeReference.getTime() ? 1 : 0;
    }
}
