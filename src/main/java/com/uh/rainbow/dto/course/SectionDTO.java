package com.uh.rainbow.dto.course;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.uh.rainbow.entities.Instructor;

import java.util.List;

/**
 * Section DTO with section details
 *
 * @param crn           Course reference number
 * @param sectionNumber Section number of course
 * @param instructor    Instructor of section
 * @param curEnrolled   Number of students enrolled
 * @param maxEnrolled   Section capacity
 * @param curWaitlist   Number of students waitlist
 * @param maxWaitlist   Waitlist capacity
 * @param attributes    Course attributes
 * @param descriptions  Additional course descriptions
 * @param notes         Additional notes
 * @param meetings      List of meetings for this section
 */
@JsonPropertyOrder({"sectionNumber"})
public record SectionDTO(int crn, String sectionNumber,
                         Instructor instructor,
                         int curEnrolled, int maxEnrolled,
                         int curWaitlist, int maxWaitlist,
                         List<String> attributes, List<String> descriptions, List<String> notes,
                         List<MeetingDTO> meetings) {
}
