package com.uh.rainbow.service;

import com.uh.rainbow.banner.*;
import com.uh.rainbow.dto.course.CourseDTO;
import com.uh.rainbow.dto.identifier.IdentifierDTO;
import com.uh.rainbow.entities.Course;
import com.uh.rainbow.entities.Section;
import com.uh.rainbow.exception.InvalidCampusCodeException;
import com.uh.rainbow.exception.InvalidTermCodeException;
import com.uh.rainbow.filter.CourseFilter;
import com.uh.rainbow.log.Logger;
import com.uh.rainbow.log.MessageBuilder;
import com.uh.rainbow.request.CourseFilterRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <b>File:</b> CampusService.java
 * <p>
 * <b>Description:</b> Service loading University of Hawai'i campus codes
 *
 * @author Derek Garcia
 */

@Getter
@Service
public class CampusService {

    private static final Logger LOGGER = new Logger(BannerAPIService.class);

    private final List<IdentifierDTO> campuses;
    private final Map<String, String> campusLookup;
    private final Map<String, String> subjectLookup;
    private final Map<String, Map<String, Set<String>>> campusSubjectsByTerm;
    private final BannerAPIService bannerAPIService;
    private final CourseFilterMapper courseFilterMapper;
    private List<IdentifierDTO> terms;
    private boolean bannerSubjectsQueried;

    /**
     * Create new campus service
     *
     * @param objectMapper       Jackson object mapper
     * @param campusesFile       File path to campuses json file
     * @param bannerAPIService   Banner9 API service
     * @param courseFilterMapper Mapper for course filter requests
     * @throws IOException if fail to find campus json file
     */
    public CampusService(ObjectMapper objectMapper,
                         @Value("${rainbow.data.campuses-file}") Resource campusesFile,
                         BannerAPIService bannerAPIService,
                         CourseFilterMapper courseFilterMapper) throws IOException {
        try (InputStream is = campusesFile.getInputStream()) {
            this.campuses = objectMapper.readValue(is, new TypeReference<>() {
            });
        }
        this.campusLookup = campuses.stream().collect(Collectors.toMap(IdentifierDTO::id, IdentifierDTO::value));
        this.subjectLookup = new HashMap<>();
        this.campusSubjectsByTerm = new HashMap<>();
        this.bannerSubjectsQueried = false;

        this.bannerAPIService = bannerAPIService;
        this.courseFilterMapper = courseFilterMapper;
    }


    /**
     * Check if the cache is valid
     */
    private void loadCache() {
        if (bannerSubjectsQueried) {
            LOGGER.debug(new MessageBuilder(MessageBuilder.Type.BANNER).addDetails("Using cached data"));
            return;
        }

        LOGGER.info(new MessageBuilder(MessageBuilder.Type.BANNER).addDetails("No cached data, fetching. . ."));
        Instant start = Instant.now();
        List<SubjectsResponse> subjectsObjects = bannerAPIService.fetchSubjects();
        LOGGER.info(new MessageBuilder(MessageBuilder.Type.BANNER).addDetails("Fetched data").setDuration(start));

        Set<IdentifierDTO> terms = new HashSet<>();
        // parse response
        if (subjectsObjects != null) {
            for (SubjectsResponse obj : subjectsObjects) {
                terms.add(obj.toTermIdentifier());
                // todo - save in database to either
                // a. get subjects by term (and where they are offered)
                subjectLookup.put(obj.stvsubjCode(), obj.stvsubjDesc());
                // todo get number of courses offered
                campusSubjectsByTerm
                        .computeIfAbsent(obj.ssbsectCampCode(), k -> new HashMap<>())
                        .computeIfAbsent(obj.stvtermCode(), k -> new HashSet<>())
                        .add(obj.stvsubjCode());
            }
            // update cache
            this.terms = new ArrayList<>(terms);
        }
        bannerSubjectsQueried = true;
    }

    /**
     * Apply a filter to a list of courses
     *
     * @param request Request to build filter from
     * @return List of filtered courses
     */
    private List<Course> filterCourses(CourseFilterRequest request, List<Course> courses) {
        CourseFilter filter = courseFilterMapper.toFilter(request);
        List<Course> validCourses = new ArrayList<>();
        for (Course c : courses) {
            if (filter.rejectCourse(c))
                continue;
            // prune invalid sections
            c.getSections().values().removeIf(filter::rejectSection);
            if (!c.getSections().isEmpty())
                validCourses.add(c);
        }
        return validCourses;
    }

    /**
     * Fetch all terms if not already cached
     * todo - maybe move to own service?
     *
     * @return List of terms available
     */
    public List<IdentifierDTO> fetchTerms() {
        loadCache();
        return terms;
    }


    /**
     * Fetch subjects for a campus and term if not already cached
     *
     * @param instID Campus code
     * @param termID Term code
     * @return List of subject identifiers offered at the given campus and term
     * @throws InvalidCampusCodeException If the provided campus code is invalid
     * @throws InvalidTermCodeException   If the provided term code is invalid
     */
    public List<IdentifierDTO> fetchSubjects(String instID, String termID) throws InvalidCampusCodeException, InvalidTermCodeException {
        loadCache();
        // validate campus
        Map<String, Set<String>> termSubjects = campusSubjectsByTerm.get(instID.toUpperCase());
        if (termSubjects == null)
            throw new InvalidCampusCodeException(instID);

        // validate term
        Set<String> subjectCodes = termSubjects.get(termID);
        if (subjectCodes == null)
            throw new InvalidTermCodeException(termID);

        // convert to identifiers
        return subjectCodes.stream()
                .map(s -> new IdentifierDTO(s, subjectLookup.get(s)))
                .collect(Collectors.toList());
    }

    /**
     * Fetch all courses for subjects
     *
     * @param instID              Campus code
     * @param termID              Term code
     * @param subjectIDs          List of subjects to fetch courses for
     * @param detailed            Return detailed course information
     * @param courseFilterRequest DTO with filter options
     * @return List of courses that match the filter
     */
    public List<? extends CourseDTO> fetchCourses(String instID, String termID, List<String> subjectIDs, boolean detailed, CourseFilterRequest courseFilterRequest) {
        // todo validate codes
        if (subjectIDs == null || subjectIDs.isEmpty())
            subjectIDs = new ArrayList<>(campusSubjectsByTerm.get(instID.toUpperCase()).get(termID));
        // todo replace with async
        // todo check if request needed against cache

        Map<String, Course> courseLookup = new HashMap<>();
        for (String subjectID : subjectIDs) {
            // todo - wrap in batch request
            // course details
            List<CoursesResponse> crl = bannerAPIService.fetchCourses(instID, termID, subjectID, true);
            List<CourseDescResponse> cdrl = bannerAPIService.fetchCourseDescriptions(instID, termID, subjectID, true);
            List<SectionDescResponse> sdrl = bannerAPIService.fetchSectionDescriptions(instID, termID, subjectID, true);
            List<SectionNotesResponse> snrl = bannerAPIService.fetchSectionNotes(instID, termID, subjectID, true);
            List<SectionAttribResponse> sarl = bannerAPIService.fetchSectionAttributes(instID, termID, subjectID, true);
            // Section details
            List<BaseSectionResponse> bsrl = bannerAPIService.fetchSectionInstructors(instID, termID, subjectID, true);
            List<SectionCountsResponse> scrl = bannerAPIService.fetchSectionCounts(instID, termID, subjectID, true);
            List<MeetingsResponse> mrl = bannerAPIService.fetchMeetings(instID, termID, subjectID, true);

            Map<String, Course> crnCourseLookup = new HashMap<>();

            // map courses
            for (CoursesResponse cr : crl) {
                String key = "%s_%s".formatted(cr.subjCode(), cr.formatCourseNumber());
                // create course if dne
                courseLookup.putIfAbsent(key, cr.toCourse());
                // map crn to course
                crnCourseLookup.put(cr.ssbsectCrn1(), courseLookup.get(key));
            }

            // add course details
            cdrl.forEach((cdr -> crnCourseLookup.get(cdr.ssbsectCrn1()).setDescription(cdr.textNarrative())));
            sdrl.forEach((cdr -> crnCourseLookup.get(cdr.crn()).addDescription(cdr.text())));
            snrl.forEach((cdr -> crnCourseLookup.get(cdr.crn()).addNote(cdr.textNarrative())));
            sarl.forEach((cdr -> crnCourseLookup.get(cdr.ssbsectCrn()).addAttribute(cdr.desc())));

            // map sections
            Map<String, Section> sectionLookup = bsrl.stream()
                    .collect(Collectors.toMap(
                            BaseSectionResponse::ssbsectCrn,    // key
                            BaseSectionResponse::toSection,     // value
                            (existing, duplicate) -> existing  // keep first on collision
                    ));
            // add section details
            scrl.forEach((scr -> sectionLookup.get(scr.crn()).setEnrollmentCounts(scr.enrl(), scr.maxEnrl(), scr.waitCount(), scr.waitCapacity())));
            mrl.forEach((m) -> {
                sectionLookup.get(m.ssbsectCrn()).addMeetings(m.toMeetings());
                Course c = crnCourseLookup.get(m.ssbsectCrn());
                c.setStartDate(m.formatStartDate());
                c.setEndDate(m.formatEndDate());
            });

            // Add sections to courses
            sectionLookup.forEach((crn, s) -> crnCourseLookup.get(crn).addSection(s));

            // todo - store in cache
        }

        // todo - load from cache

        // apply filter if provided
        List<Course> courses = (courseFilterRequest == null)
                ? new ArrayList<>(courseLookup.values())
                : filterCourses(courseFilterRequest, courseLookup.values().stream().toList());

        // convert to dto
        return detailed
                ? courses.stream().map(Course::toDetailedCourseDTO).toList()
                : courses.stream().map(Course::toSimpleCourseDTO).toList();
    }


}
