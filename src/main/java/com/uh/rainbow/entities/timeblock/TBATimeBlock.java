package com.uh.rainbow.entities.timeblock;

/**
 * <b>File:</b> TBATimeBlock.java
 * <p>
 * <b>Description:</b> Special TimeBlock with a "TBA" time
 *
 * @author Derek Garcia
 */
public class TBATimeBlock extends TimeBlock_I {
    /**
     * Creates default TBA block
     *
     * @param startDate Start Date of class
     * @param endDate End Date of class
     */
    protected TBATimeBlock(String startDate, String endDate) {
        super("TBA", startDate, "TBA", endDate);
    }
}
