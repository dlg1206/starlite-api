package com.uh.rainbow.banner;

/**
 * <b>File:</b> SubjectDTO.java
 * <p>
 * <b>Description:</b> DTO for relevant fields from <a href="https://www.sis.hawaii.edu:9350/crseavail/api/subjects">...</a> response
 *
 * @author Derek Garcia
 */
public record SubjectDTO(String stvtermDesc, String stvtermCode) {
}
