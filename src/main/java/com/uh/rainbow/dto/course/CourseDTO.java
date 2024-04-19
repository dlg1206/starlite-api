package com.uh.rainbow.dto.course;

import com.uh.rainbow.dto.section.SectionDTO;
import com.uh.rainbow.entities.Course;
import com.uh.rainbow.entities.Section;
import com.uh.rainbow.util.SourceURL;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>File:</b> CourseDTO.java
 * <p>
 * <b>Description:</b> Course DTO
 *
 * @author Derek Garcia
 */
public record CourseDTO(String cid, String name, String source, String credits, List<SectionDTO> sections) {

    /**
     * Create new course without any sections
     *
     * @param source Source URL of course data
     */
    public CourseDTO(SourceURL source, String cid, String name, String credits) {
        this(cid, name, source.toString(), credits, new ArrayList<>());
    }

    /**
     * Convert stored Section DTOs into Section objects
     *
     * @return List of converted sections
     */
    public List<Section> toSections() {
        List<Section> sections = new ArrayList<>();
        Course course = new Course(this.cid, this.name, this.credits);
        this.sections.forEach((s) -> sections.add(new Section(course, s)));
        return sections;
    }
}
