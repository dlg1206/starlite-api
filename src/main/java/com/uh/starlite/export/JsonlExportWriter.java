package com.uh.starlite.export;

import com.uh.starlite.dto.*;
import com.uh.starlite.entities.Course;
import com.uh.starlite.entities.Section;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
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
    private final LinkedHashSet<CourseRecord> courseSet;
    private final LinkedHashSet<SectionRecord> sectionSet;
    private final LinkedHashSet<MeetingRecord> meetingSet;
    private final LinkedHashSet<InstructorRecord> instructorSet;

    // metadata
    private final ReentrantLock timeLock;
    private final ObjectMapper mapper;
    private LocalDateTime firstWrite;
    private LocalDateTime lastWrite;

    /**
     * Create new jsonl export writer
     */
    public JsonlExportWriter() {
        this.timeLock = new ReentrantLock();
        this.campusSet = new LinkedHashSet<>();
        this.termSet = new LinkedHashSet<>();
        this.subjectSet = new LinkedHashSet<>();
        this.courseSet = new LinkedHashSet<>();
        this.sectionSet = new LinkedHashSet<>();
        this.meetingSet = new LinkedHashSet<>();
        this.instructorSet = new LinkedHashSet<>();
        this.mapper = JsonMapper.builder()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
    }


    /**
     * Update the first and last write times
     */
    private void updateWriteTime() {
        timeLock.lock();
        try {
            if (firstWrite == null)
                firstWrite = LocalDateTime.now();
        } finally {
            lastWrite = LocalDateTime.now();
            timeLock.unlock();
        }
    }

    /**
     * Convert metadata into JSON byte array
     *
     * @return JSON byte array
     */
    private byte[] metaToByteArray() {
        timeLock.lock();
        try {
            ByteArrayOutputStream metaBuf = new ByteArrayOutputStream();
            mapper.writeValue(metaBuf, Map.of("start", firstWrite, "end", lastWrite));
            metaBuf.write('\n');
            return metaBuf.toByteArray();
        } finally {
            // reset writes
            firstWrite = null;
            lastWrite = null;
            timeLock.unlock();
        }

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
            buf.write('\n');
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
        files.put("metadata.json", metaToByteArray());
        files.put("campuses.jsonl", toByteArray(campusSet));
        files.put("terms.jsonl", toByteArray(termSet));
        files.put("subjects.jsonl", toByteArray(subjectSet));
        files.put("courses.jsonl", toByteArray(courseSet));
        files.put("sections.jsonl", toByteArray(sectionSet));
        files.put("meetings.jsonl", toByteArray(meetingSet));
        files.put("instructors.jsonl", toByteArray(instructorSet));
        return files;
    }

    /**
     * Format and write campuses
     *
     * @param campuses List of campus identifiers
     */
    @Override
    public void writeCampuses(List<IdentifierDTO> campuses) {
        campuses.forEach(i -> campusSet.add(new CampusRecord(i.id(), i.value())));
        updateWriteTime();
    }

    /**
     * Format and write terms and subjects
     *
     * @param offerings List of subject offerings at campuses
     */
    @Override
    public void writeOfferings(List<OfferingDTO> offerings) {
        offerings.forEach(o -> {
            termSet.add(new TermRecord(o.termCode(), o.termName()));
            subjectSet.add(new SubjectRecord(o.campusCode(), o.termCode(), o.subjectCode(), o.subjectName()));
        });
        updateWriteTime();
    }

    /**
     * Format and write courses
     *
     * @param campusCode  Campus code
     * @param termCode    Term code
     * @param subjectCode Subject code
     * @param courses     List of courses
     */
    @Override
    public void writeCourse(String campusCode, String termCode, String subjectCode, List<Course> courses) {
        for (Course c : courses) {
            for (Section s : c.getSections().values()) {
                s.getMeetings().forEach(m -> meetingSet.add(m.toMeetingRecord(s.getCrn())));
                if (s.getInstructor() != null)
                    instructorSet.add(s.getInstructor().toInstructorRecord(s.getCrn()));
                sectionSet.add(s.toSectionRecord(c.getCourseID()));
            }
            courseSet.add(c.toCourseRecord(campusCode, termCode));
        }
        updateWriteTime();
    }

    /**
     * Close and export data
     *
     * @return Export
     */
    @Override
    public byte[] write() throws IOException {
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
     * @param campus  Campus code
     * @param term    Term code
     * @param subject Subject code
     * @param name    Name of subject
     */
    private record SubjectRecord(String campus, String term, String subject, String name) {
    }
}
