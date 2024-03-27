package com.uh.rainbow.util;

import com.uh.rainbow.entities.timeblock.TimeBlock;
import com.uh.rainbow.entities.timeblock.simple.SimpleTime;

import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <b>File:</b> CourseFilter.java
 * <p>
 * <b>Description:</b> Filters courses when parsing tables. Default is accept all
 *
 * @author Derek Garcia
 */
public class CourseFilter {


    /**
     * Builder for Course Filter
     */
    public static class Builder {

        private Set<String> crns = null;
        private Set<String> codes = null;
        private Set<String> subjects = null;
        private SimpleTime startAfter = null;
        private SimpleTime endAfter= null;
        private int online = -1;
        private Set<String> instructors = null;
        private List<String> keywords = null;

        /**
         * Set Course Reference numbers
         *
         * @param crns Course Reference numbers
         * @return CourseFilterBuilder
         */
        public Builder setCRNs(List<String> crns){
            if(crns != null)
                this.crns = new HashSet<>(crns);
            return this;
        }

        /**
         * Set course numbers ( 101, 301, etc )
         *
         * @param codes Course numbers
         * @return CourseFilterBuilder
         */
        public Builder setCourseNumbers(List<String> codes){
            if(codes != null)
                this.codes = new HashSet<>(codes);
            return this;
        }

        /**
         * Set subjects ( ICS, FIRE, etc )
         *
         * @param subjects Subjects
         * @return CourseFilterBuilder
         */
        public Builder setSubjects(List<String> subjects){
            if(subjects != null){
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
        public Builder setStartAfter(String startAfter){
            try{
                if(startAfter != null)
                    this.startAfter = new SimpleTime(startAfter);
            } catch (ParseException ignored){
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
        public Builder setEndBefore(String endBefore){
            try{
                if(endBefore != null)
                    this.endAfter = new SimpleTime(endBefore);
            } catch (ParseException ignored){
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
        public Builder setOnline(String online){
            if(online != null)
                this.online = Boolean.parseBoolean(online) ? 1 : 0;
            return this;
        }

        /**
         * Set list of instructors to search for
         *
         * @param instructors list of instructors to search for
         * @return CourseFilterBuilder
         */
        public Builder setInstructors(List<String> instructors){
            if(instructors != null){
                instructors.replaceAll(String::toLowerCase);
                this.instructors = new HashSet<>(instructors);
            }
            return this;
        }

        /**
         * Set list of keywords to search for in the Course titles
         *
         * @param keywords list of keywords to search for
         * @return CourseFilterBuilder
         */
        public Builder setKeywords(List<String> keywords){
            if(keywords != null){
                keywords.replaceAll(String::toLowerCase);
                this.keywords = keywords;
            }
            return this;
        }

        /**
         * Build Course filter
         *
         * @return Course Filter
         */
        public CourseFilter build(){
            return new CourseFilter(crns, codes, subjects, startAfter, endAfter, online, instructors, keywords);
        }
    }

    private final Set<String> crns;
    private final Set<String> codes;
    private final Set<String> subjects;
    private final SimpleTime startAfter;
    private final SimpleTime endBefore;
    private final int online;
    private final Set<String> instructors;
    private final List<String> keywords;

    /**
     * Create new course filter
     *
     * @param crns Set of Course Reference Numbers
     * @param codes Set of Course codes
     * @param subjects Set of subjects
     * @param startAfter Earliest time a class can start
     * @param endBefore Latest time a class can end at
     * @param online Boolean whether to include or exclude online classes
     * @param instructors List of instructors to search for
     * @param keywords List of keywords to search for
     */
    private CourseFilter(
            Set<String> crns,
            Set<String> codes,
            Set<String> subjects,
            SimpleTime startAfter,
            SimpleTime endBefore,
            int online,
            Set<String> instructors,
            List<String> keywords){

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
     * Check if CRN is valid
     *
     * @param crn Course reference number to validate
     * @return true if found, false otherwise
     */
    public boolean validCRN(String crn){
        // Default accept
        if(this.crns == null)
            return true;
        return this.crns.contains(crn);
    }

    /**
     * Check if Course ID ( ie ICS 101 ) is valid
     *
     * @param cid Course ID to validate
     * @return true if found, false otherwise
     */
    public boolean validCID(String cid){
        // default accept
        if(this.codes == null)
            return true;
        return this.subjects.contains(cid.split(" ")[0]) || this.codes.contains(cid.split(" ")[1]);
    }

    /**
     * Check if Subject ( ICS ) is valid
     *
     * @param subject Subject to validate
     * @return true if found, false otherwise
     */
    public boolean validSubject(String subject){
        // default accept
        if(this.subjects == null)
            return true;
        return this.subjects.contains(subject.toUpperCase());
    }

    /**
     * Check if times ( 1000-1230 ) are valid. Will check against start and end times if any
     *
     * @param times Time string in the form HHmm-HHmm
     * @return true if found, false otherwise
     */
    public boolean validTimes(String times){
        // default accept
        if(this.startAfter == null && this.endBefore == null)
            return true;

        // default accept TBA classes
        if(times.equalsIgnoreCase("TBA"))
            return true;

        try{
            TimeBlock timeBlock = new TimeBlock(times, "TBA");

            // If start after != null, fail if the start time is before the earliest start
            if(this.startAfter != null && timeBlock.getStartTime().beforeOrEqual(this.startAfter) == 1)
                return false;

            // If end before != null, fail if the end time is after the latest end
            if(this.endBefore != null && timeBlock.getEndTime().afterOrEqual(this.endBefore) == 1)
                return false;

            // ok
            return true;
        } catch (ParseException e){
            return false;
        }
    }

    /**
     * Check if room is "online"
     *
     * @param room Room to check
     * @return true if found, false otherwise
     */
    public boolean validOnline(String room){
        // Default accept
        if(this.online == -1)
            return true;

        int isOnline = room.toLowerCase().contains("online") ? 1 : 0;
        return this.online == isOnline;
    }

    /**
     * Check if instructor is valid
     *
     * @param instructor Instructor to look for
     * @return true if found, false otherwise
     */
    public boolean validInstructor(String instructor){
        // Default accept
        if(this.instructors == null)
            return true;
        return this.instructors.contains(instructor.toLowerCase());
    }

    /**
     * Check if string contains any keywords
     *
     * @param string String to search for keywords
     * @return true if found, false otherwise
     */
    public boolean keywordsMatch(String string){
        // Default accept
        if(this.keywords == null)
            return true;

        // Search through keywords for match
        for(String keyword : this.keywords){
            // Match a keyword
            if(string.contains(keyword))
                return true;
        }
        // No keywords matches
        return false;
    }
}
