package com.uh.starlite.filter;

import com.uh.starlite.entities.Course;
import com.uh.starlite.entities.Meeting;
import com.uh.starlite.entities.Section;
import com.uh.starlite.enums.Day;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.uh.starlite.util.Util.pluralS;

/**
 * <b>File:</b> CourseFilter.java
 * <p>
 * <b>Description:</b> Filters for courses, sections, and meetings
 *
 * @author Derek Garcia
 */
public class CourseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CourseFilter.class);

    private final Set<Integer> acceptCRNs;
    private final Set<Integer> rejectCRNs;
    private final RegexFilter courseNumberFilter;
    private final RegexFilter courseIDFilter;
    private final Set<Day> acceptDays;
    private final Set<Day> rejectDays;
    private final LocalTime startAfter;
    private final LocalTime endBefore;
    private final Boolean onlyOnline;
    private final Boolean onlyAsync;
    private final Boolean hasMajorRestriction;
    private final Boolean hasPrerequisites;
    private final Boolean canAudit;
    private final Boolean excludeFull;
    private final Boolean excludeWaitlist;
    private final Set<String> acceptInstructors;
    private final Set<String> rejectInstructors;
    private final RegexFilter titleKeywordFilter;
    private final RegexFilter descKeywordFilter;

    private final boolean skipCourseValidation;
    private final boolean skipSectionValidation;
    private final boolean skipMeetingValidation;

    /**
     * Create new course filter
     *
     * @param acceptCRNs          Set of Course reference numbers to exclusively include
     * @param rejectCRNs          Set of Course reference numbers to exclusively exclude
     * @param courseNumberFilter  Filter for accept and reject course levels
     * @param courseIDFilter      Filter for accept and reject specific course IDs
     * @param acceptDays          Days a section must occur on
     * @param rejectDays          Days a section can't occur on
     * @param startAfter          Earliest time a class can start
     * @param endBefore           Latest time a class can end at
     * @param onlyOnline          Whether to include or exclude exclusively online classes
     * @param onlyAsync           Whether to include or exclude exclusively online sync classes
     * @param hasMajorRestriction Whether to include or exclude exclusively classes with major restrictions
     * @param hasPrerequisite     Whether to include or exclude exclusively classes with prereqs
     * @param canAudit            Whether to include or exclude exclusively classes with an audit option
     * @param excludeFull         Whether to include or exclude exclusively completely full classes
     * @param excludeWaitlist     Whether to include or exclude exclusively completely classes with a waitlist
     * @param acceptInstructors   Instructors to exclusively allow
     * @param rejectInstructors   Instructors to exclusively reject
     * @param titleKeywordFilter  Filter for accept and reject keywords in the course title
     * @param descKeywordFilter   Filter for accept and reject keywords in the course description
     */
    private CourseFilter(
            Set<Integer> acceptCRNs,
            Set<Integer> rejectCRNs,
            RegexFilter courseNumberFilter,
            RegexFilter courseIDFilter,
            Set<Day> acceptDays,
            Set<Day> rejectDays,
            LocalTime startAfter,
            LocalTime endBefore,
            Boolean onlyOnline,
            Boolean onlyAsync,
            Boolean hasMajorRestriction,
            Boolean hasPrerequisite,
            Boolean canAudit,
            Boolean excludeFull,
            Boolean excludeWaitlist,
            Set<String> acceptInstructors,
            Set<String> rejectInstructors,
            RegexFilter titleKeywordFilter,
            RegexFilter descKeywordFilter
    ) {

        this.acceptCRNs = acceptCRNs;
        this.rejectCRNs = rejectCRNs;
        this.courseNumberFilter = courseNumberFilter;
        this.courseIDFilter = courseIDFilter;
        this.acceptDays = acceptDays;
        this.rejectDays = rejectDays;
        this.startAfter = startAfter;
        this.endBefore = endBefore;
        this.onlyOnline = onlyOnline;
        this.onlyAsync = onlyAsync;
        this.hasMajorRestriction = hasMajorRestriction;
        this.hasPrerequisites = hasPrerequisite;
        this.canAudit = canAudit;
        this.excludeFull = excludeFull;
        this.excludeWaitlist = excludeWaitlist;
        this.acceptInstructors = acceptInstructors;
        this.rejectInstructors = rejectInstructors;
        this.titleKeywordFilter = titleKeywordFilter;
        this.descKeywordFilter = descKeywordFilter;

        // precompute skips
        this.skipCourseValidation = (courseNumberFilter == null
                && courseIDFilter == null
                && titleKeywordFilter == null
                && descKeywordFilter == null
                && hasPrerequisite == null
                && canAudit == null
                && hasMajorRestriction == null);
        this.skipSectionValidation = (acceptCRNs == null
                && rejectCRNs == null
                && acceptInstructors == null
                && rejectInstructors == null
                && excludeFull == null
                && excludeWaitlist == null);
        this.skipMeetingValidation = (acceptDays == null
                && rejectDays == null
                && startAfter == null
                && endBefore == null
                && onlyOnline == null
                && onlyAsync == null);
    }

    /**
     * Check if the meeting should be rejected
     * If no meeting filters are used, will default to false
     *
     * @param meeting Meeting to validate
     * @return true if reject, false otherwise
     */
    private boolean rejectMeeting(Meeting meeting) {
        if (skipMeetingValidation)
            return false;

        // onlyOnline == true: reject in person classes
        // onlyOnline == false: reject online classes
        if (onlyOnline != null && meeting.isOnline() != onlyOnline)
            return true;

        // onlyAsync == true: reject sync classes
        // onlyAsync == false: reject async classes
        if (onlyAsync != null && meeting.isAsync() != onlyAsync)
            return true;

        // acceptDays == null && rejectDays == null && startAfter == null && endBefore == null && onlyOnline == null && onlyAsync == null
        // reject if not on a requested day
        if (acceptDays != null && !acceptDays.contains(meeting.getDay()))
            return true;

        if (rejectDays != null && rejectDays.contains(meeting.getDay()))
            return true;

        // class starts earlier than allowed
        if (startAfter != null && meeting.getStartTime() != null && meeting.getStartTime().isBefore(startAfter))
            return true;

        // class ends later than allowed
        return endBefore != null && meeting.getEndTime() != null && meeting.getEndTime().isAfter(endBefore);
    }

    /**
     * Check if the section should be rejected
     * If no section filters are used, will default to false
     *
     * @param section Section to validate
     * @return true if reject, false otherwise
     */
    private boolean rejectSection(Section section) {

        // short circuit if no section or meeting checks
        if (skipSectionValidation && skipMeetingValidation)
            return false;

        // excludeFull == true: reject section with no seats in the section and waitlist
        // excludeFull == false or null: no-op, don't filter on this
        if (Boolean.TRUE.equals(excludeFull) && section.isFull())
            return true;

        // excludeWaitlisted == true: reject section with no seats in the section but seats in the waitlist
        // excludeWaitlisted == false or null: no-op, don't filter on this
        if (Boolean.TRUE.equals(excludeWaitlist) && section.isWaitlisted())
            return true;

        // reject if not a requested crn
        if (acceptCRNs != null && !acceptCRNs.contains(section.getCrn()))
            return true;

        if (rejectCRNs != null && rejectCRNs.contains(section.getCrn()))
            return true;

        // reject if not a requested instructor
        if (acceptInstructors != null && !acceptInstructors.contains(section.getInstructor().username()))
            return true;

        if (rejectInstructors != null && rejectInstructors.contains(section.getInstructor().username()))
            return true;

        // validate meetings if not skipping
        return !skipMeetingValidation && section.getMeetings().stream().anyMatch(this::rejectMeeting);
    }

    /**
     * Check if the course should be rejected
     * If no course filters are used, will default to false
     *
     * @param course Course to validate
     * @return true if reject, false otherwise
     */
    private boolean rejectCourse(Course course) {
        if (skipCourseValidation)
            return false;

        // hasMajorRestriction == true: reject sections without a restriction
        // hasMajorRestriction == false: reject sections with a restriction
        if (hasMajorRestriction != null && course.isMajorRestriction() != hasMajorRestriction)
            return true;

        // hasPrerequisites == true: reject courseIDs without a prereq
        // hasPrerequisites == false: reject courseIDs with a prereq
        if (hasPrerequisites != null && course.hasPrerequisite() != hasPrerequisites)
            return true;

        // canAudit == true: reject courseIDs without an option to audit
        // canAudit == false: reject courseIDs with an option to audit
        if (canAudit != null && course.canAudit() != canAudit)
            return true;

        if (courseNumberFilter != null && courseNumberFilter.reject(course.getCourseID().number()))
            return true;

        if (courseIDFilter != null && courseIDFilter.reject(course.getCourseID().toString()))
            return true;

        if (titleKeywordFilter != null && titleKeywordFilter.reject(course.getName()))
            return true;

        return descKeywordFilter != null && descKeywordFilter.reject(course.getDescription());

        // accept course
    }

    /**
     * Apply a filter to a list of courses
     *
     * @param courses List of courses to filter
     * @return List of filtered courseIDs
     */
    public List<Course> filterCourses(List<Course> courses) {
        List<Course> validCourses = new ArrayList<>();
        int sectionReject = 0;
        for (Course c : courses) {
            // skip invalid course
            if (rejectCourse(c))
                continue;

            // prune invalid sections
            int before = c.getSections().size();
            c.getSections().values().removeIf(this::rejectSection);
            sectionReject += before - c.getSections().size();
            if (!c.getSections().isEmpty())
                validCourses.add(c);
        }
        LOGGER.info("Filtered out {} and {}",
                pluralS(courses.size() - validCourses.size(), "course"),
                pluralS(sectionReject, "section"));
        return validCourses;
    }

    public static class Builder {
        private Set<Integer> acceptCRNs;
        private Set<Integer> rejectCRNs;
        private RegexFilter courseNumberFilter;
        private RegexFilter courseIDFilter;
        private Set<Day> acceptDays;
        private Set<Day> rejectDays;
        private LocalTime startAfter;
        private LocalTime endBefore;
        private Boolean onlyOnline;
        private Boolean onlyAsync;
        private Boolean hasMajorRestriction;
        private Boolean hasPrerequisites;
        private Boolean canAudit;
        private Boolean excludeFull;
        private Boolean excludeWaitlist;
        private Set<String> acceptInstructors;
        private Set<String> rejectInstructors;
        private RegexFilter titleKeywordFilter;
        private RegexFilter descKeywordFilter;

        /**
         * Format a N** pattern to regex
         *
         * @param pattern Course ID pattern to format
         * @return Regex equivalent of pattern
         */
        private String formatCourseIDRegex(String pattern) {
            return pattern.strip()
                    .replace("**", "\\d{2}")
                    .replace("*", "\\d");
        }

        /**
         * Create a regex filter for course IDs
         *
         * @param accept Collection of accept course IDs
         * @param reject Collection of reject course IDs
         * @return {@link RegexFilter} for course IDs, null of both accept and reject are nell
         */
        private RegexFilter createCourseFilter(Collection<String> accept, Collection<String> reject) {
            // early reject of both null
            if (accept == null && reject == null)
                return null;
            // else format filter
            return RegexFilter.of(
                    accept == null ? List.of() : accept.stream().map(this::formatCourseIDRegex).toList(),
                    reject == null ? List.of() : reject.stream().map(this::formatCourseIDRegex).toList()
            );
        }

        /**
         * Create a regex filter
         *
         * @param accept Collection of accept strings
         * @param reject Collection of reject strings
         * @return {@link RegexFilter}, null of both accept and reject are nell
         */
        private RegexFilter createRegexFilter(Collection<String> accept, Collection<String> reject) {
            // early reject of both null
            if (accept == null && reject == null)
                return null;
            // else return filter
            return RegexFilter.of(
                    accept == null ? List.of() : accept,
                    reject == null ? List.of() : reject
            );
        }

        /**
         * Set accept CRNs
         *
         * @param v CRNs to accept
         * @return {@link Builder}
         */
        public Builder acceptCRNs(Set<Integer> v) {
            this.acceptCRNs = v;
            return this;
        }

        /**
         * Set reject CRNs
         *
         * @param v CRNs to reject
         * @return {@link Builder}
         */
        public Builder rejectCRNs(Set<Integer> v) {
            this.rejectCRNs = v;
            return this;
        }

        /**
         * Set course number filter
         * '*' wild card can be used ie 1** -> 101, 102, 110 etc.
         *
         * @param accept Course number strings to accept
         * @param reject Course number strings to rejct
         * @return {@link Builder}
         */
        public Builder courseNumberFilter(Collection<String> accept, Collection<String> reject) {
            this.courseNumberFilter = createCourseFilter(accept, reject);
            return this;
        }

        /**
         * Set course ID filter
         * '*' wild card can be used ie ICS 1** -> ICS 101, ICS 102, ICS 110 etc.
         *
         * @param accept Course ID strings to accept
         * @param reject Course ID strings to reject
         * @return {@link Builder}
         */
        public Builder courseIDFilter(Collection<String> accept, Collection<String> reject) {
            this.courseIDFilter = createCourseFilter(accept, reject);
            return this;
        }

        /**
         * Set accept days
         *
         * @param v Days to accept
         * @return {@link Builder}
         */
        public Builder acceptDays(Set<Day> v) {
            this.acceptDays = v;
            return this;
        }

        /**
         * Set reject days
         *
         * @param v Days to reject
         * @return {@link Builder}
         */
        public Builder rejectDays(Set<Day> v) {
            this.rejectDays = v;
            return this;
        }

        /**
         * Set start after time
         *
         * @param v Time to a section must start after
         * @return {@link Builder}
         */
        public Builder startAfter(LocalTime v) {
            this.startAfter = v;
            return this;
        }

        /**
         * Set end before time
         *
         * @param v Time to a section must end before
         * @return {@link Builder}
         */
        public Builder endBefore(LocalTime v) {
            this.endBefore = v;
            return this;
        }

        /**
         * Set to allow only online courses
         *
         * @param v Boolean to allow or deny only online courses
         * @return {@link Builder}
         */
        public Builder onlyOnline(Boolean v) {
            this.onlyOnline = v;
            return this;
        }

        /**
         * Set to allow only async courses
         *
         * @param v Boolean to allow or deny only async courses
         * @return {@link Builder}
         */
        public Builder onlyAsync(Boolean v) {
            this.onlyAsync = v;
            return this;
        }

        /**
         * Set to allow courses with a major restriction or not
         *
         * @param v Boolean to allow or deny courses with a major restriction
         * @return {@link Builder}
         */
        public Builder hasMajorRestriction(Boolean v) {
            this.hasMajorRestriction = v;
            return this;
        }

        /**
         * Set to allow courses with prerequisites or not
         *
         * @param v Boolean to allow or deny courses with prerequisites
         * @return {@link Builder}
         */
        public Builder hasPrerequisites(Boolean v) {
            this.hasPrerequisites = v;
            return this;
        }

        /**
         * Set to allow courses that can be audited or not
         *
         * @param v Boolean to allow or deny courses that can be audited
         * @return {@link Builder}
         */
        public Builder canAudit(Boolean v) {
            this.canAudit = v;
            return this;
        }

        /**
         * Set to allow full courses or not
         *
         * @param v Boolean to allow or deny full courses
         * @return {@link Builder}
         */
        public Builder excludeFull(Boolean v) {
            this.excludeFull = v;
            return this;
        }

        /**
         * Set to allow waitlisted courses or not
         *
         * @param v Boolean to allow or deny waitlisted courses
         * @return {@link Builder}
         */
        public Builder excludeWaitlist(Boolean v) {
            this.excludeWaitlist = v;
            return this;
        }

        /**
         * Set accept instructor usernames
         *
         * @param v instructor usernames to accept
         * @return {@link Builder}
         */
        public Builder acceptInstructors(Set<String> v) {
            this.acceptInstructors = v;
            return this;
        }

        /**
         * Set reject instructor usernames
         *
         * @param v instructor usernames to reject
         * @return {@link Builder}
         */
        public Builder rejectInstructors(Set<String> v) {
            this.rejectInstructors = v;
            return this;
        }

        /**
         * Set course title keyword filter
         *
         * @param accept Keywords to accept
         * @param reject Keywords to reject
         * @return {@link Builder}
         */
        public Builder titleKeywordFilter(Collection<String> accept, Collection<String> reject) {
            this.titleKeywordFilter = createRegexFilter(accept, reject);
            return this;
        }

        /**
         * Set course description keyword filter
         *
         * @param accept Keywords to accept
         * @param reject Keywords to reject
         * @return {@link Builder}
         */
        public Builder descKeywordFilter(Collection<String> accept, Collection<String> reject) {
            this.descKeywordFilter = createRegexFilter(accept, reject);
            return this;
        }

        /**
         * Build the course filter
         *
         * @return {@link CourseFilter}
         */
        public CourseFilter build() {
            return new CourseFilter(
                    acceptCRNs,
                    rejectCRNs,
                    courseNumberFilter,
                    courseIDFilter,
                    acceptDays,
                    rejectDays,
                    startAfter,
                    endBefore,
                    onlyOnline,
                    onlyAsync,
                    hasMajorRestriction,
                    hasPrerequisites,
                    canAudit,
                    excludeFull,
                    excludeWaitlist,
                    acceptInstructors,
                    rejectInstructors,
                    titleKeywordFilter,
                    descKeywordFilter);
        }
    }
}
