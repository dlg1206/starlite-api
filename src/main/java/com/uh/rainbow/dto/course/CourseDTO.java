package com.uh.rainbow.dto.course;

import com.uh.rainbow.dto.section.SectionDTO;
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
}
