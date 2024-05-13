package com.uh.rainbow.services;

import com.uh.rainbow.dto.course.CourseDTO;
import com.uh.rainbow.dto.meeting.MeetingDTO;
import com.uh.rainbow.dto.section.SectionDTO;
import com.uh.rainbow.entities.Meeting;
import com.uh.rainbow.entities.Section;
import com.uh.rainbow.util.SourceURL;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <b>File:</b> DTOMapperService.java
 * <p>
 * <b>Description:</b> Service that maps entities to DTO objects
 *
 * @author Derek Garcia
 */
@Service
public class DTOMapperService {

    /**
     * Map Meetings to DTOs
     *
     * @param meetings List of Meetings to map
     * @return List of Meeting DTOs
     */
    private List<MeetingDTO> toMeetingDTOs(List<Meeting> meetings){
        List<MeetingDTO> meetingDTOs = new LinkedList<>();
        for(Meeting meeting : meetings){
            meetingDTOs.add(new MeetingDTO(
                    meeting.getDay().toString(),
                    meeting.getRoom(),
                    meeting.getStartTime().toString(),
                    meeting.getEndTime().toString(),
                    meeting.getStartDate().toString(),
                    meeting.getEndDate().toString()
            ));
        }
        return meetingDTOs;
    }

    /**
     * Map section to DTO
     *
     * @param section Section to map
     * @return SectionDTO
     */
    private SectionDTO toSectionDTO(Section section){
        return new SectionDTO(
                section.getDetailsURL(),
                section.getSectionNumber(),
                Integer.parseInt(section.getCRN()),
                section.getInstructor(),
                section.getCurrEnrolled(),
                section.getSeatsAvailable(),
                section.getFailedMeetings(),
                section.getAdditionalDetails(),
                toMeetingDTOs(section.getMeetings())
        );
    }

    /**
     * Convert a list of sections into groupings of courses
     *
     * @param source Source of data
     * @param sections List of sections to group
     * @return List of CourseDTOs
     */
    public List<CourseDTO> toCourseDTOs(List<Section> sections){
        // Group Sections
        Map<String, CourseDTO> courses = new HashMap<>();
        for(Section section : sections){
            String cid = section.getCID();
            courses.putIfAbsent( cid, new CourseDTO(section.getSourceURL(), cid, section.getTitle(), section.getCredits()));
            courses.get(section.getCID()).sections().add(toSectionDTO(section));
        }

        // Sort and return results
        return courses.values().stream()
                .sorted(Comparator.comparing(CourseDTO::cid))   // sort by CID
                .toList();

    }
}
