package com.uh.rainbow.dto.section;

import com.uh.rainbow.dto.meeting.MeetingDTO;

import java.util.List;

/**
 * <b>File:</b> SectionDTO.java
 * <p>
 * <b>Description:</b> SectionDTO
 * <p>
 * TODO: v2 replace 'sid' with 'section_number'
 *
 * @author Derek Garcia
 */
public record SectionDTO(String url, String sid, int crn, String instructor, int curr_enrolled, int seats_available,
                         int failed_meetings, List<String> additional_details, List<MeetingDTO> meetings) {
}
