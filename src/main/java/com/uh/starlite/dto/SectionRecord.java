package com.uh.starlite.dto;


import com.uh.starlite.enums.SectionFormat;

import java.util.Collection;

/**
 * Section DTO record for jsonl
 *
 * @param courseUUID         UUID of course this section belongs to
 * @param crn                Course reference number
 * @param sectionNumber      Section number of course
 * @param instructorUsername UH username of the instructor for the section
 * @param curEnrolled        Number of students enrolled
 * @param maxEnrolled        Section capacity
 * @param curWaitlist        Number of students waitlist
 * @param maxWaitlist        Waitlist capacity
 * @param attributes         Course attributes
 * @param descriptions       Additional course descriptions
 * @param notes              Additional notes
 */
public record SectionRecord(String courseUUID,
                            int crn, String sectionNumber,
                            String instructorUsername,
                            SectionFormat format,
                            int curEnrolled, int maxEnrolled,
                            int curWaitlist, int maxWaitlist,
                            Collection<String> attributes, Collection<String> descriptions, Collection<String> notes) {
}
