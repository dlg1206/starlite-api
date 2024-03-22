package com.uh.rainbow.dto;

import com.uh.rainbow.entities.Section;

import java.util.List;

/**
 * <b>File:</b> CourseDTO
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public record CourseDTO(String source, int errors, String cid, String name, String credits, List<Section> sections) {
}
