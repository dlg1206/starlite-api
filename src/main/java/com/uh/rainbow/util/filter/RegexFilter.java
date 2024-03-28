package com.uh.rainbow.util.filter;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * <b>File:</b> RegexFilter.java
 * <p>
 * <b>Description:</b> Regex filter to sort between accept and reject patterns
 *
 * @author Derek Garcia
 */
public class RegexFilter {

    /**
     * Build for Regex Filter
     */
    public static class Builder {
        private final static String ACCEPT_ALL = "[\\w\\W]";
        private final static String REJECT_ALL = "[^\\w\\W]";
        private final List<String> accept = new ArrayList<>();
        private final List<String> reject = new ArrayList<>();

        /**
         * Add string to filter
         *
         * @param string String to add. Strings starting with '!' will be rejected, all else is accepted
         */
        public Builder addString(String string) {
            if (string.charAt(0) != '!') {
                this.accept.add(string);
            } else {
                this.reject.add(string.substring(1));  // strip leading '!'
            }
            return this;
        }

        /**
         * Build new RegexFilter using accept and reject lists
         *
         * @return RegexFilter
         */
        public RegexFilter build() {
            // Default accept so accept == true
            String acceptRegex = this.accept.isEmpty() ? ACCEPT_ALL : StringUtils.join(this.accept, "|");

            // Default reject all so !reject == true
            String rejectRegex = this.reject.isEmpty() ? REJECT_ALL : StringUtils.join(this.reject, "|");

            return new RegexFilter(
                    Pattern.compile(acceptRegex, Pattern.CASE_INSENSITIVE),
                    Pattern.compile(rejectRegex, Pattern.CASE_INSENSITIVE));
        }
    }

    private final Pattern accept;
    private final Pattern reject;

    /**
     * Create new Regex Filter
     *
     * @param accept Regex of patterns to accept
     * @param reject Regex of patterns to reject
     */
    private RegexFilter(Pattern accept, Pattern reject) {
        this.accept = accept;
        this.reject = reject;
    }

    /**
     * Test that the string is valid
     *
     * @param string String to test
     * @return True if accepted and not rejected, false otherwise
     */
    public boolean test(String string) {
        return this.accept.matcher(string).find() && !this.reject.matcher(string).find();
    }
}
