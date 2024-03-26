package com.uh.rainbow.entities.timeblock;

import com.uh.rainbow.entities.timeblock.simple.SimpleDate;
import com.uh.rainbow.entities.timeblock.simple.SimpleTime;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.concurrent.TimeUnit;

/**
 * <b>File:</b> TimeBlock.java
 * <p>
 * <b>Description:</b> Time Block with actual start and end times
 *
 * @author Derek Garcia
 */
public class TimeBlock {
    private SimpleTime startTime;
    private SimpleTime endTime;
    private SimpleDate startDate;
    private SimpleDate endDate;


    /**
     * Constructor that parses the UH formatted time string into a time block
     *
     * @param timeString Time string in the form: HHMM-HHMM(?:a|p) ie 0900-1200p
     * @throws ParseException Failed to parse time
     */
    public TimeBlock(String timeString, String dateString) throws ParseException {

        String[] dates = dateString.split("-");
        this.startDate = new SimpleDate(dates[0]);
        this.endDate = new SimpleDate(dates.length == 2 ? dates[1] : dates[0]);

        if(timeString.equalsIgnoreCase("TBA")){
            this.startTime = new SimpleTime(timeString);
            this.endTime = new SimpleTime(timeString);
            return;
        }
        // Get a/p and separate start and end times
        char tod = timeString.charAt(timeString.length() - 1);
        String[] times = timeString.substring(0, timeString.length() - 1).split("-");

        this.startTime = new SimpleTime(times[0]);
        this.endTime = new SimpleTime(times[1]);

        /*
        Mark start in afternoon if 'p' and before 6
        Handles edge case "0100-0200p", class doesn't start at 1 am but "0900-0200p" class probably starts at 9
        6 AM arbitrary start that assuming no classes occur before
         */

        if(tod == 'p'){
            if(this.startTime.beforeOrEqual(new SimpleTime("0600")) == 1)
                this.startTime.addHours(12);

            // Mark end in afternoon if 'p'
            if (this.endTime.beforeOrEqual(new SimpleTime("1200")) == 1)
                this.endTime.addHours(12);

            // If the class is longer than 5 hours, start is probably in am
            if(this.startTime.duration(this.endTime, TimeUnit.HOURS) > 5)
                this.startTime.addHours(12);
        }






    }

    public SimpleTime getStartTime() {
        return this.startTime;
    }

    public SimpleTime getEndTime(){
        return this.endTime;
    }

    public SimpleDate getStartDate(){
        return this.startDate;
    }

    public SimpleDate getEndDate(){
        return this.endDate;
    }
}
