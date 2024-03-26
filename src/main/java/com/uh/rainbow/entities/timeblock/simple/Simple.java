package com.uh.rainbow.entities.timeblock.simple;

import org.apache.commons.lang3.time.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * <b>File:</b> Simple.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public abstract class Simple extends Date {
    protected static final String TBA_STRING = "TBA";
    protected Date timeReference = null;    // default is null / TBA

    protected Simple(String formatString, String timeReference) throws ParseException {
        super();
        DateFormat formatter = new SimpleDateFormat(formatString);

        if (!timeReference.equalsIgnoreCase(TBA_STRING))
            this.timeReference = formatter.parse(timeReference);

    }


    public void addHours(int numHours) {
        if(this.timeReference == null)
            return;
        this.timeReference = DateUtils.addHours(this.timeReference, numHours);
    }

    public int beforeOrEqual(Simple other) {
        if (this.timeReference == null || other.timeReference == null)
            return -1;
        return this.timeReference.getTime() <= other.timeReference.getTime() ? 1 : 0;
    }


    public int afterOrEqual(Simple other) {
        if (this.timeReference == null || other.timeReference == null)
            return -1;
        return this.timeReference.getTime() >= other.timeReference.getTime() ? 1 : 0;
    }

    public long duration(Simple other, TimeUnit timeUnit){
        if(this.timeReference == null || other.timeReference == null)
            return -1;
        long diff = Math.abs(this.timeReference.getTime() - other.timeReference.getTime());
        return TimeUnit.valueOf(String.valueOf(timeUnit)).convert(diff, TimeUnit.MILLISECONDS);
    }

}
