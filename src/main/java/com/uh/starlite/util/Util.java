package com.uh.starlite.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * <b>File:</b> Util.java
 * <p>
 * <b>Description:</b> Helper util methods
 *
 * @author Derek Garcia
 */
public class Util {


    // prevent instantiation
    private Util() {
    }

    /**
     * Append 's' if appropriate
     *
     * @param quantity Quantity of subject
     * @param subject  Subject to plural
     * @return Plural subject if appropriate
     */
    public static String pluralS(int quantity, String subject) {
        String quantMsg = "%d %s".formatted(quantity, subject);
        return quantity == 1 ? quantMsg : quantMsg + "s";
    }

    /**
     * Filter a list for distinct values
     *
     * @param list List of items
     * @param <T>  Type of the result returned by the call
     * @return Unique list of times
     */
    public static <T> List<T> distinct(List<T> list) {
        return list == null ? null : new ArrayList<>(new LinkedHashSet<>(list));
    }
}
