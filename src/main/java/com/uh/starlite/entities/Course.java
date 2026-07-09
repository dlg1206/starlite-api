package com.uh.starlite.entities;

import com.uh.starlite.dto.DetailedCourseDTO;
import com.uh.starlite.dto.ScheduledCourseDTO;
import com.uh.starlite.dto.SimpleCourseDTO;
import com.uh.starlite.enums.GradingOption;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>File:</b> Course.java
 * <p>
 * <b>Description:</b> University course
 * <p>
 *
 * @author Derek Garcia
 */
public class Course {

    @Getter
    private final CourseID courseID;
    @Getter
    private final String name;
    @Getter
    private final String description;
    private final String prereqDescription;
    private final int credits;
    private final Set<GradingOption> gradingOptions;
    @Getter
    private final boolean majorRestriction;
    private final String approvalAuthority;
    private final LocalDate startDate;
    private final LocalDate endDate;
    @Getter
    private final Map<Integer, Section> sections;

    /**
     * Create new immutable course
     *
     * @param subjectCode       Subject code of course
     * @param number            Course number
     * @param name              Full name of the course
     * @param description       Description of the course
     * @param prereqDescription Description of course prerequirements
     * @param startDate         Start date of course
     * @param credits           Number of credits the course is worth
     * @param gradingOptions    Set of grading options available for this course
     * @param majorRestriction  If the selection is restricted to the major of the parent course
     * @param approvalAuthority Authority approval required to take the course
     * @param endDate           End date of course
     * @param sections          Map of sections and their course reference numbers belonging to this course
     */
    private Course(String subjectCode,
                   String number,
                   String name,
                   String description,
                   String prereqDescription,
                   int credits,
                   Set<GradingOption> gradingOptions,
                   boolean majorRestriction,
                   String approvalAuthority,
                   LocalDate startDate,
                   LocalDate endDate,
                   Map<Integer, Section> sections) {
        this.courseID = new CourseID(subjectCode, number);
        this.name = name;
        this.description = description;
        this.prereqDescription = prereqDescription;
        this.credits = credits;
        this.gradingOptions = gradingOptions;
        this.majorRestriction = majorRestriction;
        this.approvalAuthority = approvalAuthority;
        this.startDate = startDate;
        this.endDate = endDate;
        this.sections = sections;
    }

    /**
     * @return True if the course has a prereq, false otherise
     */
    public boolean hasPrerequisite() {
        return prereqDescription != null && !prereqDescription.isEmpty();
    }

    /**
     * @return True if can audit this course, false otherise
     */
    public boolean canAudit() {
        return gradingOptions.contains(GradingOption.AUDIT);
    }

    /**
     * Convert this course to DTO without section details
     *
     * @return {@link SimpleCourseDTO}
     */
    public SimpleCourseDTO toSimpleCourseDTO() {
        return new SimpleCourseDTO(courseID.subjectCode(), courseID.number(), name,
                description, prereqDescription,
                credits, gradingOptions.stream().map(GradingOption::getDescription).sorted().toList(),
                majorRestriction, approvalAuthority,
                startDate.toString(), endDate.toString(),
                sections.size());
    }

    /**
     * Convert this course to with section details
     *
     * @return {@link DetailedCourseDTO}
     */
    public DetailedCourseDTO toDetailedCourseDTO() {
        return new DetailedCourseDTO(courseID.subjectCode(), courseID.number(), name,
                description, prereqDescription,
                credits, gradingOptions.stream().map(GradingOption::getDescription).sorted().toList(),
                majorRestriction, approvalAuthority,
                startDate.toString(), endDate.toString(),
                sections.values().stream().map(Section::toSectionDTO).toList());
    }

    /**
     * Convert this course into a scheduled course DTO with a specific section
     *
     * @param sectionCRN Course reference number of section to included
     * @return {@link ScheduledCourseDTO}
     */
    public ScheduledCourseDTO toScheduleDTO(int sectionCRN) {
        return new ScheduledCourseDTO(courseID.subjectCode(), courseID.number(), name, description, credits,
                sections.get(sectionCRN).toSectionDTO());
    }

    public static class Builder {
        private static final Pattern prereqRegex = Pattern.compile(" Pre: (?!consent)(.*?)\\.");

        private final String subjectCode;
        private final String number;
        private final String name;
        private final int credits;
        private final Set<GradingOption> gradingOptions;
        private final Map<Integer, Section> sections;
        private String description;
        private String prereqDescription;
        @Setter
        private boolean majorRestriction;
        @Setter
        private String approvalAuthority;
        @Setter
        private LocalDate startDate;
        @Setter
        private LocalDate endDate;

        /**
         * Create new {@link Course} builder
         *
         * @param subjectCode Subject code of course
         * @param number      Course number
         * @param name        Full name of the course
         * @param credits     Number of credits the course is worth
         */
        public Builder(String subjectCode, String number, String name, int credits) {
            this.subjectCode = subjectCode;
            this.number = number;
            this.name = name;
            this.credits = credits;
            this.gradingOptions = new HashSet<>();
            this.sections = new HashMap<>();
        }

        /**
         * Set overall course description. Will extract prereq if detected
         *
         * @param description Course description
         */
        public void setDescription(String description) {
            description = description.strip();
            Matcher m = prereqRegex.matcher(description);
            // if find match, extract and remove from destining
            if (m.find()) {
                String prereqDescription = m.group(1).strip();
                this.prereqDescription = prereqDescription.substring(0, 1).toUpperCase() + prereqDescription.substring(1);
                description = description.replace(m.group(), "");
            }
            this.description = description;
        }

        /**
         * Add a grading option for this course
         *
         * @param gradingOption {@link GradingOption}
         */
        public void addGradingOption(GradingOption gradingOption) {
            this.gradingOptions.add(gradingOption);
        }

        /**
         * Add a section to this course
         *
         * @param section Section
         */
        public void addSection(Section section) {
            sections.put(section.getCrn(), section);
        }

        /**
         * Create new immutable course
         *
         * @return {@link Course}
         */
        public Course build() {
            return new Course(
                    subjectCode,
                    number,
                    name,
                    description,
                    prereqDescription,
                    credits,
                    gradingOptions,
                    majorRestriction,
                    approvalAuthority,
                    startDate,
                    endDate,
                    sections);
        }
    }
}
