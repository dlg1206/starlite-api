package com.uh.rainbow.util.filter;

import com.uh.rainbow.entities.Meeting;
import com.uh.rainbow.entities.Section;
import com.uh.rainbow.entities.time.simple.SimpleTime;
import com.uh.rainbow.util.logging.Logger;
import com.uh.rainbow.util.logging.MessageBuilder;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>File:</b> CourseFilter.java
 * <p>
 * <b>Description:</b> Filters courses when parsing tables. Default is accept all
 *
 * @author Derek Garcia
 */
public class CourseFilter {
    private final static Logger LOGGER = new Logger(CourseFilter.class);

    /**
     * Builder for Course Filter
     */
    public static class Builder {
        // Utility record for matching full courses
        private record FullCoursePattern(Set<String> subjects, Pattern regex){}
        private Set<String> crns;
        private Pattern codes;
        private Set<String> subjects;
        private FullCoursePattern fullCourses;     // will override codes and subjects
        private RegexFilter days;
        private SimpleTime startAfter;
        private SimpleTime endAfter;
        private int online = -1;
        private int synchronous = -1;
        private RegexFilter instructors;
        private RegexFilter keywords;

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
         * Set course numbers ( 101, 301, etc )
         * Wild cards can also be used ie 1** will return and 100 level course
         *
         * @param codes Course numbers
         * @return CourseFilterBuilder
         */
        public Builder setCourseNumbers(List<String> codes) {
            if (codes != null) {
                // replace * with regex numbers
                String regex = StringUtils.join(codes, "|")
                        .replace("**", "[0-9]{2}")
                        .replace("*", "[0-9]");

                this.codes = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            }
            return this;
        }


        /**
         * Set specific courses ( ICS 101, ZOOL 420, etc )
         * Will override any previous values set in subject or codes
         * Wild cards can also be used ie ICS 1** will return and ISC 100 level courses
         *
         * @param fullCourses List of specific courses to filter
         * @return CourseFilterBuilder
         */
        public Builder setFullCourses(List<String> fullCourses){
            if (fullCourses != null) {
                // Create new patter matching exact cases
                String regex = StringUtils.join(fullCourses, "|")
                        .replace(" ", "")
                        .replace("**", "[0-9]{2}")
                        .replace("*", "[0-9]");
                this.fullCourses = new FullCoursePattern(new HashSet<>(), Pattern.compile(regex, Pattern.CASE_INSENSITIVE));

                // Save all subjects to filter
                fullCourses.replaceAll(String::toUpperCase);
                Pattern subject = Pattern.compile("([a-z]*)", Pattern.CASE_INSENSITIVE);
                for(String course : fullCourses){
                    Matcher m = subject.matcher(course);
                    if(!m.find())
                        continue;
                    this.fullCourses.subjects.add(m.group(1));
                }
            }
            return this;
        }

        /**
         * Set list of days for meetings using UH day codes.
         * Prepending with '!' to inverse the search
         * ie "!M" -> sections not on monday
         *
         * @param days list of days to filter by
         * @return CourseFilterBuilder
         */
        public Builder setDays(List<String> days) {
            if (days != null) {
                RegexFilter.Builder builder = new RegexFilter.Builder();
                // + "{1}$" ensures only one occurrence of string
                days.forEach((d) -> {
                    d = d + "{1}$";
                    builder.addString(d);
                });
                this.days = builder.build();
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
                LOGGER.error(new MessageBuilder(MessageBuilder.Type.COURSE).addDetails("Failed to Parse time string '%s'".formatted(startAfter)));
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
                LOGGER.error(new MessageBuilder(MessageBuilder.Type.COURSE).addDetails("Failed to Parse time string '%s'".formatted(startAfter)));
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
         * Set synchronous preference
         *
         * @param sync Boolean string to indicate synchronous preference. 1 syn, 0 sync, default both
         * @return CourseFilterBuilder
         */
        public Builder setSynchronous(String sync) {
            if (sync != null)
                this.synchronous = Boolean.parseBoolean(sync) ? 1 : 0;

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
                RegexFilter.Builder builder = new RegexFilter.Builder();
                instructors.forEach(builder::addString);
                this.instructors = builder.build();
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
                RegexFilter.Builder builder = new RegexFilter.Builder();
                keywords.forEach(builder::addString);
                this.keywords = builder.build();
            }
            return this;
        }

        /**
         * Build Course filter
         *
         * @return Course Filter
         */
        public CourseFilter build() {
            // Filter using generic subjects and codes
            if(this.fullCourses == null)
                return new CourseFilter(crns, codes, subjects, null, days, startAfter, endAfter, online, synchronous, instructors, keywords);

            // Filter by specific courses
            return new CourseFilter(crns, null, fullCourses.subjects, fullCourses.regex, days, startAfter, endAfter, online, synchronous, instructors, keywords);
        }
    }

    private final Set<String> crns;
    private final Pattern codes;
    private final Set<String> subjects;
    private final Pattern fullCourses;
    private final RegexFilter days;
    private final SimpleTime startAfter;
    private final SimpleTime endBefore;
    private final int online;
    private final int synchronous;
    private final RegexFilter instructors;
    private final RegexFilter keywords;
    /**
     * Create new course filter
     *
     * @param crns        Set of Course Reference Numbers
     * @param codes       Set of Course codes
     * @param subjects    Set of subjects
     * @param fullCourses Regex of specific courses to match by
     * @param days        Regex of days to filter by
     * @param startAfter  Earliest time a class can start
     * @param endBefore   Latest time a class can end at
     * @param online      Boolean whether to include or exclude online classes ( default both )
     * @param synchronous Boolean whether to include or exclude online sync classes ( default both )
     * @param instructors Regex of instructors to search for
     * @param keywords    Regex of keywords to search for
     */
    private CourseFilter(
            Set<String> crns,
            Pattern codes,
            Set<String> subjects,
            Pattern fullCourses,
            RegexFilter days,
            SimpleTime startAfter,
            SimpleTime endBefore,
            int online,
            int synchronous,
            RegexFilter instructors,
            RegexFilter keywords) {

        this.crns = crns;
        this.codes = codes;
        this.subjects = subjects;
        this.fullCourses = fullCourses;
        this.days = days;
        this.startAfter = startAfter;
        this.endBefore = endBefore;
        this.online = online;
        this.synchronous = synchronous;
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
        if (!(
                validCRN(section.getCRN()) &&
                validCID(section.getCID()) &&
                validInstructor(section.getInstructor()) &&
                keywordsMatch(section.getTitle())))
            return false;

        // Validate meeting details
        int numOnline = 0;
        int numSync = 0;
        for (Meeting m : section.getMeetings()) {
            // Fail immediately if violate time
            if (!(validDay(m.getDay().toCode()) && validStartTime(m.getStartTime()) && validEndTime(m.getEndTime())))
                return false;

            // update meeting type counts
            String lowerRoom = m.getRoom().toLowerCase();
            numOnline += lowerRoom.contains("online") ? 1 : 0;
            numSync += !lowerRoom.contains("asynchronous") ? 1 : 0;     // +1 if in-person / online sync

        }

        // Fail if fail meeting validation
        int totalMeetings = section.getMeetings().size();
        return validMeetingType(this.online, numOnline, totalMeetings) && validMeetingType(this.synchronous, numSync, totalMeetings);
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
        if (this.subjects == null && this.codes == null && this.fullCourses == null)
            return true;

        // reject if doesn't match specific course
        if(this.fullCourses != null && !this.fullCourses.matcher(cid.replace(" ", "")).find())
            return false;

        // reject if missing from subjects
        if (this.subjects != null && !this.subjects.contains(cid.split(" ")[0]))
            return false;

        // reject if missing from codes
        if(this.codes != null && !this.codes.matcher(cid.split(" ")[1]).find())
            return false;

        return true;
    }

    /**
     * Check if day is valid
     *
     * @param day day to look for
     * @return true if found, false otherwise
     */
    private boolean validDay(String day) {
        // Default accept
        if (this.days == null)
            return true;
        // Attempt to match day
        return this.days.test(day);
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
     * Check if all meetings meet the meeting type preference
     * 0: exclude all
     * 1: exclusively meeting type
     * default: accept all
     *
     * @param preference    int preference for the meeting
     * @param meetingCount  Number of meetings that match the preference
     * @param totalMeetings Total number of meetings for section
     * @return true if found, false otherwise
     */
    private boolean validMeetingType(int preference, int meetingCount, int totalMeetings) {
        switch (preference) {
            case 0 -> {
                // No meetings of this type
                return meetingCount == 0;
            }
            case 1 -> {
                // all meetings online
                return meetingCount == totalMeetings;
            }
            default -> {
                // accept both
                return true;
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
        return this.instructors.test(instructor);
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
        return this.keywords.test(string);
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

    /**
     * Builder for Course Filter
     */
    public static class Builder {
        private Set<String> crns;
        private Pattern codes;
        private Set<String> subjects;
        private RegexFilter days;
        private SimpleTime startAfter;
        private SimpleTime endAfter;
        private int online = -1;
        private int synchronous = -1;
        private RegexFilter instructors;
        private RegexFilter keywords;

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
            if (codes != null) {
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
         * Set list of days for meetings using UH day codes.
         * Prepending with '!' to inverse the search
         * ie "!M" -> sections not on monday
         *
         * @param days list of days to filter by
         * @return CourseFilterBuilder
         */
        public Builder setDays(List<String> days) {
            if (days != null) {
                RegexFilter.Builder builder = new RegexFilter.Builder();
                // + "{1}$" ensures only one occurrence of string
                days.forEach((d) -> {
                    d = d + "{1}$";
                    builder.addString(d);
                });
                this.days = builder.build();
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
                LOGGER.error(new MessageBuilder(MessageBuilder.Type.COURSE).addDetails("Failed to Parse time string '%s'".formatted(startAfter)));
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
                LOGGER.error(new MessageBuilder(MessageBuilder.Type.COURSE).addDetails("Failed to Parse time string '%s'".formatted(startAfter)));
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
         * Set synchronous preference
         *
         * @param sync Boolean string to indicate synchronous preference. 1 syn, 0 sync, default both
         * @return CourseFilterBuilder
         */
        public Builder setSynchronous(String sync) {
            if (sync != null)
                this.synchronous = Boolean.parseBoolean(sync) ? 1 : 0;

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
                RegexFilter.Builder builder = new RegexFilter.Builder();
                instructors.forEach(builder::addString);
                this.instructors = builder.build();
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
                RegexFilter.Builder builder = new RegexFilter.Builder();
                keywords.forEach(builder::addString);
                this.keywords = builder.build();
            }
            return this;
        }

        /**
         * Build Course filter
         *
         * @return Course Filter
         */
        public CourseFilter build() {
            return new CourseFilter(crns, codes, subjects, days, startAfter, endAfter, online, synchronous, instructors, keywords);
        }
    }
}
