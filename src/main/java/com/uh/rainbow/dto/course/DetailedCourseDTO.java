package com.uh.rainbow.dto.course;

import java.util.Comparator;
import java.util.List;

/**
 * Course DTO with course and section details
 *
 * @param subjectCode       Subject code
 * @param courseNumber      Course number
 * @param name              Name of course
 * @param description       Description of course
 * @param prereqDescription Prereqs for the course
 * @param credits           Total credits
 * @param gradingOptions    List of grading options available for this course
 * @param majorRestriction  If the selection is restricted to the major of the parent course
 * @param approvalAuthority Authority approval required to take the course
 * @param startDate         Start date of course
 * @param endDate           End date of course
 * @param sections          List of sections of the course
 */
public record DetailedCourseDTO(String subjectCode, String courseNumber, String name,
                                String description, String prereqDescription,
                                int credits, List<String> gradingOptions,
                                boolean majorRestriction, String approvalAuthority,
                                String startDate, String endDate,
                                List<SectionDTO> sections) implements CourseDTO {

    // custom comparator that extracts number from section number since can include letters
    private static final Comparator<SectionDTO> BY_SECTION_NUMBER = Comparator
            .comparingInt((SectionDTO s) -> {
                String num = s.sectionNumber();
                int i = 0;
                while (i < num.length() && Character.isDigit(num.charAt(i))) i++;
                return Integer.parseInt(num.substring(0, i));
            }).thenComparing(SectionDTO::sectionNumber);

    // compact constructor - normalizes/sorts sections on construction
    public DetailedCourseDTO {
        sections = sections.stream()
                .sorted(BY_SECTION_NUMBER)
                .toList();
    }

    /**
     * @return Course subject code
     */
    @Override
    public String getSubjectCode() {
        return subjectCode;
    }

    /**
     * @return Get course number
     */
    @Override
    public String getCourseNumber() {
        return courseNumber;
    }
}