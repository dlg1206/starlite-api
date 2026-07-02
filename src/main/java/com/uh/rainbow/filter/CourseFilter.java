package com.uh.rainbow.filter;

import com.uh.rainbow.entities.Course;
import com.uh.rainbow.entities.Meeting;
import com.uh.rainbow.entities.Section;
import com.uh.rainbow.enums.Day;

import java.time.LocalTime;
import java.util.Set;

/**
 * <b>File:</b> CourseFilter.java
 * <p>
 * <b>Description:</b> Filter for courses and sections
 *
 * @author Derek Garcia
 */
public class CourseFilter {
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
     * @param acceptInstructors   Instructors to exclusively allow
     * @param rejectInstructors   Instructors to exclusively reject
     * @param titleKeywordFilter  Filter for accept and reject keywords in the course title
     * @param descKeywordFilter   Filter for accept and reject keywords in the course description
     */
    public CourseFilter(
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
        this.acceptInstructors = acceptInstructors;
        this.rejectInstructors = rejectInstructors;
        this.titleKeywordFilter = titleKeywordFilter;
        this.descKeywordFilter = descKeywordFilter;

        // precompute skips
        this.skipCourseValidation = (courseNumberFilter == null && courseIDFilter == null && titleKeywordFilter == null && descKeywordFilter == null && hasPrerequisite == null && canAudit == null && hasMajorRestriction == null);
        this.skipSectionValidation = (acceptCRNs == null && rejectCRNs == null && acceptInstructors == null && rejectInstructors == null);
        this.skipMeetingValidation = (acceptDays == null && rejectDays == null && startAfter == null && endBefore == null && onlyOnline == null && onlyAsync == null);
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
    public boolean rejectSection(Section section) {

        // short circuit if no section or meeting checks
        if (skipSectionValidation && skipMeetingValidation)
            return false;

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
    public boolean rejectCourse(Course course) {
        if (skipCourseValidation)
            return false;

        // hasMajorRestriction == true: reject sections without a restriction
        // hasMajorRestriction == false: reject sections with a restriction
        if (hasMajorRestriction != null && course.isMajorRestriction() != hasMajorRestriction)
            return true;

        // hasPrerequisites == true: reject courses without a prereq
        // hasPrerequisites == false: reject courses with a prereq
        if (hasPrerequisites != null && course.hasPrerequisite() != hasPrerequisites)
            return true;

        // canAudit == true: reject courses without an option to audit
        // canAudit == false: reject courses with an option to audit
        if (canAudit != null && course.canAudit() != canAudit)
            return true;

        if (courseNumberFilter != null && courseNumberFilter.reject(course.getNumber()))
            return true;

        if (courseIDFilter != null && courseIDFilter.reject(course.getCourseID()))
            return true;

        if (titleKeywordFilter != null && titleKeywordFilter.reject(course.getName()))
            return true;

        return descKeywordFilter != null && descKeywordFilter.reject(course.getDescription());

        // accept course
    }
}
