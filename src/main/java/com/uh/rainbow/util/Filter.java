package com.uh.rainbow.util;

import com.uh.rainbow.entities.Meeting;
import com.uh.rainbow.entities.Section;
import com.uh.rainbow.entities.timeblock.simple.SimpleTime;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * <b>File:</b> CourseFilter.java
 * <p>
 * <b>Description:</b> Filters courses when parsing tables. Default is accept all
 *
 * @author Derek Garcia
 */
public class Filter {


    /**
     * Builder for Course Filter
     */
    public static class Builder {

        private Set<String> crns = null;
        private Pattern codes = null;
        private Set<String> subjects = null;
        private SimpleTime startAfter = null;
        private SimpleTime endAfter = null;
        private int online = -1;
        private Pattern instructors = null;
        private Pattern keywords = null;

        /**
         * Set Course Reference numbers
         *
         * @param crns Course Reference numbers
         * @return CourseFilterBuilder
         */
        public Builder setCRNs(List<String> crns) {
            if (crns != null)
                this.crns = new HashSet<>(crns);
            return this;
        }

        /**
         * Set course numbers ( 101, 301, etc )
         * Wild cards can also be used ie 1** will return and 100 level course
         *
         * @param codes Course numbers
         * @return CourseFilterBuilder
         */
        public Builder setCourseNumbers(List<String> codes) {
            if (codes != null){
                // replace * with regex numbers
                String regex = StringUtils.join(codes, "|")
                        .replace("**", "[0-9]{2}")
                        .replace("*", "[0-9]");

                this.codes = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            }
            return this;
        }

        /**
         * Set subjects ( ICS, FIRE, etc )
         *
         * @param subjects Subjects
         * @return CourseFilterBuilder
         */
        public Builder setSubjects(List<String> subjects) {
            if (subjects != null) {
                subjects.replaceAll(String::toUpperCase);
                this.subjects = new HashSet<>(subjects);
            }
            return this;
        }

        /**
         * Set start after time
         *
         * @param startAfter Earliest a class can start / time class must start after
         * @return CourseFilterBuilder
         */
        public Builder setStartAfter(String startAfter) {
            try {
                if (startAfter != null)
                    this.startAfter = new SimpleTime(startAfter);
            } catch (ParseException ignored) {
                // todo add logger
            }
            return this;
        }

        /**
         * Set end before time
         *
         * @param endBefore Latest a class can end / time class must end before
         * @return CourseFilterBuilder
         */
        public Builder setEndBefore(String endBefore) {
            try {
                if (endBefore != null)
                    this.endAfter = new SimpleTime(endBefore);
            } catch (ParseException ignored) {
                // todo add logger
            }
            return this;
        }

        /**
         * Set online preference
         *
         * @param online Boolean string to include online classes
         * @return CourseFilterBuilder
         */
        public Builder setOnline(String online) {
            if (online != null)
                this.online = Boolean.parseBoolean(online) ? 1 : 0;
            return this;
        }

        /**
         * Set list of instructors to search for
         *
         * @param instructors list of instructors to search for
         * @return CourseFilterBuilder
         */
        public Builder setInstructors(List<String> instructors) {
            if (instructors != null) {
                instructors.replaceAll(String::toLowerCase);
                this.instructors = Pattern.compile(StringUtils.join(instructors, "|"), Pattern.CASE_INSENSITIVE);
            }
            return this;
        }

        /**
         * Set list of keywords to search for in the Course titles
         *
         * @param keywords list of keywords to search for
         * @return CourseFilterBuilder
         */
        public Builder setKeywords(List<String> keywords) {
            if (keywords != null) {
                keywords.replaceAll(String::toLowerCase);
                this.keywords = Pattern.compile(StringUtils.join(keywords, "|"), Pattern.CASE_INSENSITIVE);
            }
            return this;
        }

        /**
         * Build Course filter
         *
         * @return Course Filter
         */
        public Filter build() {
            return new Filter(crns, codes, subjects, startAfter, endAfter, online, instructors, keywords);
        }
    }

    private final Set<String> crns;
    private final Pattern codes;
    private final Set<String> subjects;
    private final SimpleTime startAfter;
    private final SimpleTime endBefore;
    private final int online;
    private final Pattern instructors;
    private final Pattern keywords;

    /**
     * Create new course filter
     * todo async / sync, days of week
     *
     * @param crns        Set of Course Reference Numbers
     * @param codes       Set of Course codes
     * @param subjects    Set of subjects
     * @param startAfter  Earliest time a class can start
     * @param endBefore   Latest time a class can end at
     * @param online      Boolean whether to include or exclude online classes
     * @param instructors List of instructors to search for
     * @param keywords    List of keywords to search for
     */
    private Filter(
            Set<String> crns,
            Pattern codes,
            Set<String> subjects,
            SimpleTime startAfter,
            SimpleTime endBefore,
            int online,
            Pattern instructors,
            Pattern keywords) {

        this.crns = crns;
        this.codes = codes;
        this.subjects = subjects;
        this.startAfter = startAfter;
        this.endBefore = endBefore;
        this.online = online;
        this.instructors = instructors;
        this.keywords = keywords;
    }

    /**
     * Validate Section against filters
     *
     * @param section Section to test
     * @return True if pass, false otherwise
     */
    public boolean validSection(Section section) {

        // Validate section details
        if (!(validCRN(section.getCRN()) && validCID(section.getCID()) && validInstructor(section.getInstructor()) && keywordsMatch(section.getName())))
            return false;

        // Validate meeting details
        int numOnline = 0;
        for (Meeting m : section.getMeetings()) {
            if (!(validStartTime(m.getStartTime()) && validEndTime(m.getEndTime())))
                return false;

            numOnline += m.getRoom().toLowerCase().contains("online") ? 1 : 0;
        }

        // Fail if only online meetings
        return validOnline(numOnline, section.getMeetings().size());

        // pass all checks
    }


    /**
     * Check if CRN is valid
     *
     * @param crn Course reference number to validate
     * @return true if found, false otherwise
     */
    private boolean validCRN(String crn) {
        // Default accept
        if (this.crns == null)
            return true;
        return this.crns.contains(crn);
    }

    /**
     * Check if Course ID ( ie ICS 101 ) is valid
     *
     * @param cid Course ID to validate
     * @return true if found, false otherwise
     */
    private boolean validCID(String cid) {
        // default accept
        if (this.subjects == null && this.codes == null)
            return true;

        if (this.subjects != null && !this.subjects.contains(cid.split(" ")[0]))
            return false;

        if(this.codes != null && !this.codes.matcher(cid.split(" ")[1]).find())
            return false;

        return true;
    }

    /**
     * Check if Start Time is valid.
     *
     * @param startTime Simple start time
     * @return true if valid, false otherwise
     */
    private boolean validStartTime(SimpleTime startTime) {
        // default accept
        if (this.startAfter == null)
            return true;

        // fail if the start time is before the earliest start
        return startTime.beforeOrEqual(this.startAfter) != 1;
    }


    /**
     * Check if End Time is valid.
     *
     * @param endTime Simple end time
     * @return true if valid, false otherwise
     */
    private boolean validEndTime(SimpleTime endTime) {
        // default accept
        if (this.endBefore == null)
            return true;

        // fail if the end time is after the latest end
        return endTime.afterOrEqual(this.endBefore) != 1;
    }

    /**
     * Check if only online meetings
     *
     * @param numOnlineMeetings Number of online meetings for section
     * @param totalMeetings     Total number of meetings for section
     * @return true if found, false otherwise
     */
    private boolean validOnline(int numOnlineMeetings, int totalMeetings) {
        // Default accept
        if (this.online == -1)
            return true;

        switch (this.online) {
            case 0 -> {
                return numOnlineMeetings != totalMeetings;
            }    // at least 1 meeting not online
            case 1 -> {
                return numOnlineMeetings == totalMeetings;
            }    // all meetings online
            default -> {
                return false;
            }
        }
    }

    /**
     * Check if instructor is valid
     *
     * @param instructor Instructor to look for
     * @return true if found, false otherwise
     */
    private boolean validInstructor(String instructor) {
        // Default accept
        if (this.instructors == null)
            return true;
        // Attempt to match instructor
        return this.instructors.matcher(instructor).find();
    }

    /**
     * Check if string contains any keywords
     *
     * @param string String to search for keywords
     * @return true if found, false otherwise
     */
    private boolean keywordsMatch(String string) {
        // Default accept
        if (this.keywords == null)
            return true;

        // Attempt to match keywords
        return this.keywords.matcher(string).find();
    }

    /**
     * Check if Subject ( ICS ) is valid
     *
     * @param subject Subject to validate
     * @return true if found, false otherwise
     */
    public boolean validSubject(String subject) {
        // default accept
        if (this.subjects == null)
            return true;
        return this.subjects.contains(subject.toUpperCase());
    }
}
