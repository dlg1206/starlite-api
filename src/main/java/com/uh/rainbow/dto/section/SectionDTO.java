package com.uh.rainbow.dto.section;

import com.uh.rainbow.dto.meeting.MeetingDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>File:</b> SectionDTO.java
 * <p>
 * <b>Description:</b> SectionDTO
 *
 * @author Derek Garcia
 */
public record SectionDTO(String sid, int crn, String instructor, int curr_enrolled, int seats_available,
                         Integer failed_meetings, List<String> additional_details, List<MeetingDTO> meetings) {
    /**
     * Create new Section DTO with no meetings
     *
     * @param sid                Section ID
     * @param crn                Course Reference Number
     * @param instructor         Section instructor
     * @param curr_enrolled      Number of students currently enrolled
     * @param seats_available    Number of seats available
     * @param failed_meetings    Number of meetings failed to parse
     * @param additional_details additional details for the section
     */
    public SectionDTO(String sid, int crn, String instructor, int curr_enrolled, int seats_available, int failed_meetings, List<String> additional_details) {
        this(sid, crn, instructor, curr_enrolled, seats_available, failed_meetings == 0 ? null : 1, additional_details, new ArrayList<>());
    }
}
