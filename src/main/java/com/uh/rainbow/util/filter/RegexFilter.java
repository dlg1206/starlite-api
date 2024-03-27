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
class RegexFilter{

    /**
     * Build for Regex Filter
     */
    public static class Builder{
        private final List<String> accept = new ArrayList<>();
        private final List<String> reject = new ArrayList<>();

        /**
         * Add string to filter
         *
         * @param string String to add. Strings starting with '!' will be rejected, all else is accepted
         */
        public void addString(String string){
            // + "{1}$" ensures only one occurrence of string
            if (string.charAt(0) != '!') {
                this.accept.add(string + "{1}$");
            } else {
                this.reject.add(string.substring(1) + "{1}$");  // strip leading '!'
            }
        }

        /**
         * Build new RegexFilter using accept and reject lists
         *
         * @return RegexFilter
         */
        public RegexFilter build(){
            return new RegexFilter(
                    Pattern.compile(StringUtils.join(this.accept, "|"), Pattern.CASE_INSENSITIVE),
                    Pattern.compile(StringUtils.join(this.reject, "|"), Pattern.CASE_INSENSITIVE));
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
    private RegexFilter(Pattern accept, Pattern reject){
        this.accept = accept;
        this.reject = reject;
    }

    /**
     * Test that the string is valid
     *
     * @param string String to test
     * @return True if accepted and not rejected, false otherwise
     */
    public boolean test(String string){
        return this.accept.matcher(string).find() && !this.reject.matcher(string).find();
    }
}
