package com.uh.rainbow.entities;

import java.util.List;

/**
 * Create a buffer for a block of time
 *
 * @param spans List of reserved times for this block
 */
public record TimeBuffer(List<ReservedTime> spans) implements TimeBlock {

    /**
     * @return List of reserved times this buffer has
     */
    @Override
    public List<? extends TimeSpan> getSpans() {
        return spans;
    }
}
