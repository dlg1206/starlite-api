package com.uh.starlite.export;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uh.starlite.dto.*;
import com.uh.starlite.entities.*;
import com.uh.starlite.enums.Day;
import com.uh.starlite.enums.SectionFormat;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <b>File:</b> JsonlExportWriter.java
 * <p>
 * <b>Description:</b> Export data as a collection of jsonl files
 *
 * @author Derek Garcia
 */
public class JsonlExportWriter implements ExportWriter {

    // local cache
    private final LinkedHashSet<CampusRecord> campusSet;
    private final LinkedHashSet<TermRecord> termSet;
    private final LinkedHashSet<SubjectRecord> subjectSet;
    private final LinkedHashSet<OfferingRecord> offeringSet;
    private final LinkedHashSet<CourseRecord> courseSet;
    private final LinkedHashSet<SectionRecord> sectionSet;
    private final LinkedHashSet<MeetingRecord> meetingSet;
    private final LinkedHashSet<InstructorRecord> instructorSet;

    private final ObjectMapper mapper;

    /**
     * Create new jsonl export writer
     */
    public JsonlExportWriter() {
        this.campusSet = new LinkedHashSet<>();
        this.termSet = new LinkedHashSet<>();
        this.subjectSet = new LinkedHashSet<>();
        this.offeringSet = new LinkedHashSet<>();
        this.courseSet = new LinkedHashSet<>();
        this.sectionSet = new LinkedHashSet<>();
        this.meetingSet = new LinkedHashSet<>();
        this.instructorSet = new LinkedHashSet<>();
        this.mapper = JsonMapper.builder()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
    }


    /**
     * Flush a cache into jsonl byte array
     *
     * @param set Cache set to flush into byte array
     * @return jsonl byte array
     */
    private byte[] toByteArray(LinkedHashSet<? extends Record> set) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        Iterator<? extends Record> it = set.iterator();
        // pop and write
        while (it.hasNext()) {
            mapper.writeValue(buf, it.next());
            it.remove();
            // no extra newline at EOF
            if (it.hasNext()) buf.write('\n');
        }
        return buf.toByteArray();
    }

    /**
     * Map set caches to jsonl files
     *
     * @return Map of file names and byte content
     */
    private Map<String, byte[]> toFileMap() {
        Map<String, byte[]> files = new LinkedHashMap<>();
        files.put("campuses.jsonl", toByteArray(campusSet));
        files.put("terms.jsonl", toByteArray(termSet));
        files.put("subjects.jsonl", toByteArray(subjectSet));
        files.put("offerings.jsonl", toByteArray(offeringSet));
        files.put("courses.jsonl", toByteArray(courseSet));
        files.put("sections.jsonl", toByteArray(sectionSet));
        files.put("meetings.jsonl", toByteArray(meetingSet));
        files.put("instructors.jsonl", toByteArray(instructorSet));
        return files;
    }

    /**
     * Format and write data to sets
     *
     * @param data List of complete course offerings
     */
    private void writeData(List<IdentifierDTO> campuses, List<CompleteOfferingDTO> data) {
        campuses.forEach(i -> campusSet.add(new CampusRecord(i.id(), i.value())));
        for (CompleteOfferingDTO o : data) {
            // add all courses
            for (Course c : o.courses()) {
                // add all sections
                for (Section s : c.getSections().values()) {
                    // add all meetings
                    s.getMeetings().stream()
                            .map(Meeting::toMeetingDTO)
                            .forEach(m -> meetingSet.add(new MeetingRecord(s.getCrn(), m)));
                    // add instructor if exists
                    if (s.getInstructor() != null)
                        instructorSet.add(new InstructorRecord(s.getInstructor()));
                    // add section
                    sectionSet.add(new SectionRecord(c.getCourseID(), s.toSectionDTO()));
                }
                // add course
                courseSet.add(new CourseRecord(o.campusCode(), o.termCode(), c.getCourseID(), c.toDetailedCourseDTO()));
            }
            // add to other maps
            termSet.add(new TermRecord(o.termCode(), o.termName()));
            subjectSet.add(new SubjectRecord(o.subjectCode(), o.subjectName()));
            offeringSet.add(new OfferingRecord(o.campusCode(), o.termCode(), o.subjectCode()));
        }
    }

    /**
     * Close and export data
     *
     * @param campuses List of campuses
     * @param data     List of complete course offerings
     * @return Export bytes
     */
    @Override
    public byte[] write(List<IdentifierDTO> campuses, List<CompleteOfferingDTO> data) throws IOException {
        // write data
        writeData(campuses, data);
        // export as zip bytes
        ByteArrayOutputStream zipBuf = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zipBuf)) {
            // zip files
            for (Map.Entry<String, byte[]> entry : toFileMap().entrySet()) {
                zos.putNextEntry(new ZipEntry(entry.getKey()));
                zos.write(entry.getValue());
                zos.closeEntry();
            }
        }
        return zipBuf.toByteArray();
    }

    /**
     * Internal record for campus
     *
     * @param campus Campus code
     * @param name   Name of campus
     */
    private record CampusRecord(String campus, String name) {
    }

    /**
     * Internal record for term
     *
     * @param term Term code
     * @param name Name of term
     */
    private record TermRecord(String term, String name) {
    }

    /**
     * Internal subject record
     *
     * @param subject Subject code
     * @param name    Name of subject
     */
    private record SubjectRecord(String subject, String name) {
    }

    /**
     * Internal offering record
     *
     * @param campus  Campus code
     * @param term    Term code
     * @param subject Subject code
     */
    private record OfferingRecord(String campus, String term, String subject) {
    }

    /**
     * Course record for jsonl
     *
     * @param courseUUID        UUID for course
     * @param campus            Campus code
     * @param term              Term code
     * @param subject           Subject code
     * @param number            Course number
     * @param name              Name of course
     * @param description       Description of course
     * @param credits           Total credits
     * @param gradingOptions    List of grading options available for this course
     * @param majorRestriction  If the selection is restricted to the major of the parent course
     * @param approvalAuthority Authority approval required to take the course
     * @param startDate         Start date of course
     * @param endDate           End date of course
     */
    private record CourseRecord(String courseUUID,
                                String campus, String term, String subject,
                                String number, String name,
                                String description, String prereqDescription,
                                int credits, List<String> gradingOptions,
                                boolean majorRestriction, String approvalAuthority,
                                LocalDate startDate, LocalDate endDate) {

        /**
         * Create new record
         *
         * @param campusCode Campus code
         * @param termCode   Term code
         * @param cid        {@link CourseID}
         * @param dcd        {@link DetailedCourseDTO}
         */
        public CourseRecord(String campusCode, String termCode, CourseID cid, DetailedCourseDTO dcd) {
            this(cid.uuid(),
                    campusCode, termCode, dcd.subjectCode(), dcd.courseNumber(),
                    dcd.name(), dcd.description(), dcd.prereqDescription(),
                    dcd.credits(), dcd.gradingOptions(),
                    dcd.majorRestriction(), dcd.approvalAuthority(),
                    dcd.startDate(), dcd.endDate());
        }

    }

    /**
     * Section DTO record for jsonl
     *
     * @param courseUUID         UUID of course this section belongs to
     * @param crn                Course reference number
     * @param number             Section number of course
     * @param instructorUsername UH username of the instructor for the section
     * @param curEnrolled        Number of students enrolled
     * @param maxEnrolled        Section capacity
     * @param curWaitlist        Number of students waitlist
     * @param maxWaitlist        Waitlist capacity
     * @param attributes         Course attributes
     * @param descriptions       Additional course descriptions
     * @param notes              Additional notes
     */
    private record SectionRecord(String courseUUID,
                                 int crn, String number,
                                 String instructorUsername,
                                 SectionFormat format,
                                 int curEnrolled, int maxEnrolled,
                                 int curWaitlist, int maxWaitlist,
                                 Collection<String> attributes, Collection<String> descriptions,
                                 Collection<String> notes) {

        /**
         * Convert this section into a jsonl record map
         *
         * @param courseID ID of course this section belongs to
         * @param s        {@link SectionDTO}
         */
        public SectionRecord(CourseID courseID, SectionDTO s) {
            this(courseID.uuid(),
                    s.crn(), s.sectionNumber(),
                    s.instructor() == null ? null : s.instructor().username(),
                    s.format(),
                    s.curEnrolled(), s.maxEnrolled(),
                    s.curWaitlist(), s.maxWaitlist(),
                    s.attributes(), s.descriptions(), s.notes()
            );
        }
    }

    /**
     * Meeting record for jsonl
     *
     * @param crn      CRN of section this meeting belongs to
     * @param day      Day of week meeting occurs
     * @param start    Start time of meeting in HHmm
     * @param end      End time of meeting in HHmm
     * @param building Building code
     * @param room     Room code
     */
    private record MeetingRecord(int crn,
                                 Day day,
                                 @JsonFormat(pattern = "HHmm") LocalTime start,
                                 @JsonFormat(pattern = "HHmm") LocalTime end,
                                 String building, String room) {

        /**
         * Create new record
         *
         * @param crn CRN of section this meeting belongs to
         * @param m   {@link MeetingDTO}
         */
        public MeetingRecord(int crn, MeetingDTO m) {
            this(crn, m.day(), m.startTime(), m.endTime(), m.buildingCode(), m.roomCode());
        }
    }

    /**
     * Instructor record for jsonl
     *
     * @param instructorUsername Instructor UH username
     * @param firstName          Instructor first name
     * @param middleInitial      Instructor middle intentional
     * @param lastName           Instructor last name
     */
    private record InstructorRecord(String instructorUsername, String firstName, String middleInitial,
                                    String lastName) {
        /**
         * Create new record
         *
         * @param i {@link Instructor}
         */
        public InstructorRecord(Instructor i) {
            this(i.username(), i.firstName(), i.middleInitial(), i.lastName());
        }
    }


}
