package com.uh.rainbow.service;

import com.uh.rainbow.banner.*;
import com.uh.rainbow.dto.course.CourseDTO;
import com.uh.rainbow.entities.Course;
import com.uh.rainbow.entities.Section;
import com.uh.rainbow.filter.CourseFilter;
import com.uh.rainbow.request.CourseFilterRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <b>File:</b> CampusService.java
 * <p>
 * <b>Description:</b> Service loading University of Hawai'i campus codes
 *
 * @author Derek Garcia
 */

@Service
public class CourseService {

    private final CodeLookupService codeLookupService;
    private final BannerAPIService bannerAPIService;
    private final CourseFilterMapper courseFilterMapper;

    /**
     * Create new course service
     *
     * @param codeLookupService  Service for fetching campus and term codes
     * @param bannerAPIService   Banner9 API service
     * @param courseFilterMapper Mapper for course filter requests
     */
    public CourseService(CodeLookupService codeLookupService, BannerAPIService bannerAPIService, CourseFilterMapper courseFilterMapper) {
        this.codeLookupService = codeLookupService;
        this.bannerAPIService = bannerAPIService;
        this.courseFilterMapper = courseFilterMapper;
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
            subjectIDs = codeLookupService.lookupSubjectCodes(instID, termID);
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
