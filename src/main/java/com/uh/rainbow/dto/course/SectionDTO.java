package com.uh.rainbow.dto.course;


import com.uh.rainbow.entities.Instructor;
import com.uh.rainbow.enums.SectionFormat;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Section DTO with section details
 *
 * @param crn               Course reference number
 * @param sectionNumber     Section number of course
 * @param instructor        Instructor of section
 * @param curEnrolled       Number of students enrolled
 * @param maxEnrolled       Section capacity
 * @param curWaitlist       Number of students waitlist
 * @param maxWaitlist       Waitlist capacity
 * @param attributes        Course attributes
 * @param majorRestriction  If the selection is restricted to the major of the parent course
 * @param approvalAuthority Authority approval required to take the course
 * @param descriptions      Additional course descriptions
 * @param notes             Additional notes
 * @param meetings          List of meetings for this section
 */
public record SectionDTO(int crn, String sectionNumber,
                         Instructor instructor,
                         SectionFormat format,
                         int curEnrolled, int maxEnrolled,
                         int curWaitlist, int maxWaitlist,
                         boolean majorRestriction, String approvalAuthority,
                         Collection<String> attributes, Collection<String> descriptions, Collection<String> notes,
                         List<MeetingDTO> meetings) {

    // comparator that sorts meetings by the day of the week, then starting time
    private static final Comparator<MeetingDTO> BY_MEETING_TIME = Comparator
            .comparing(MeetingDTO::day)
            .thenComparing(MeetingDTO::startTime);

    // compact constructor - normalizes/sorts meetings on construction
    public SectionDTO {
        meetings = meetings.stream()
                .sorted(BY_MEETING_TIME)
                .toList();
    }
}
