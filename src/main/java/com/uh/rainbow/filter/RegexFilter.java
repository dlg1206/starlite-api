package com.uh.rainbow.filter;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * <b>File:</b> RegexFilter.java
 * <p>
 * <b>Description:</b> Regex filter to sort between accept and reject patterns
 *
 * @author Derek Garcia
 */
public class RegexFilter {

    private final Pattern acceptPattern;
    private final Pattern rejectPattern;

    /**
     * Create new Regex Filter
     *
     * @param acceptPattern Regex of patterns to accept
     * @param rejectPattern Regex of pattern to reject
     */
    private RegexFilter(Pattern acceptPattern, Pattern rejectPattern) {
        this.acceptPattern = acceptPattern;
        this.rejectPattern = rejectPattern;
    }

    /**
     * Build new RegexFilter using accept and reject lists
     *
     * @return RegexFilter
     */
    public static RegexFilter of(Collection<String> accept, Collection<String> reject) {
        return new RegexFilter(
                accept.isEmpty() ? null : Pattern.compile(String.join("|", accept), Pattern.CASE_INSENSITIVE),
                reject.isEmpty() ? null : Pattern.compile(String.join("|", reject), Pattern.CASE_INSENSITIVE));
    }

    /**
     * Test that the string is valid.
     * String must match acceptPattern (if set) and must NOT match rejectPattern (if set).
     *
     * @param string String to test
     * @return True if string should be rejected, false otherwise
     */
    public boolean reject(String string) {
        if (acceptPattern != null && !acceptPattern.matcher(string).find())
            return true;

        return rejectPattern != null && rejectPattern.matcher(string).find();
    }
}
