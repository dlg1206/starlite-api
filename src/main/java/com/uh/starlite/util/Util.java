package com.uh.starlite.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
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

    /**
     * Get SHA-256 message digest
     *
     * @param items Optional seed items for the digest. Order matters, same items differ order will return different digests
     * @return SHA-256 message digest
     */
    public static MessageDigest getDigestInstance(Object... items) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            Arrays.stream(items).map(String::valueOf).forEach(s -> digest.update(s.getBytes(StandardCharsets.UTF_8)));
            return digest;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
