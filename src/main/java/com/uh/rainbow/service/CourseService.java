package com.uh.rainbow.service;

import com.uh.rainbow.banner.*;
import com.uh.rainbow.dto.course.CourseDTO;
import com.uh.rainbow.entities.Course;
import com.uh.rainbow.entities.Section;
import com.uh.rainbow.filter.CourseFilter;
import com.uh.rainbow.log.Logger;
import com.uh.rainbow.log.MessageBuilder;
import com.uh.rainbow.request.CourseFilterRequest;
import com.uh.rainbow.response.CourseResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * <b>File:</b> CampusService.java
 * <p>
 * <b>Description:</b> Service loading University of Hawai'i campus codes
 *
 * @author Derek Garcia
 */

@Service
public class CourseService {

    private static final Logger LOGGER = new Logger(CourseService.class);
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
     * Fetch all course data concurrently for a single subject
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return Future with subject data
     */
    @Async("bannerTaskExecutor")
    protected CompletableFuture<SubjectResult> fetchSubjectData(String instID, String termID, String subjectID) {
        // course details
        CompletableFuture<List<CoursesResponse>> crlFuture = bannerAPIService.fetchCoursesAsync(instID, termID, subjectID, true);
        CompletableFuture<List<CourseDescResponse>> cdrlFuture = bannerAPIService.fetchCourseDescriptionsAsync(instID, termID, subjectID, true);
        CompletableFuture<List<CourseGradingResponse>> cgrlFuture = bannerAPIService.fetchCourseGradingAsync(instID, termID, subjectID, true);
        // Section details
        CompletableFuture<List<BaseSectionResponse>> bsrlFuture = bannerAPIService.fetchSectionInstructorsAsync(instID, termID, subjectID, true);
        CompletableFuture<List<SectionCountsResponse>> scrlFuture = bannerAPIService.fetchSectionCountsAsync(instID, termID, subjectID, true);
        CompletableFuture<List<SectionDescResponse>> sdrlFuture = bannerAPIService.fetchSectionDescriptionsAsync(instID, termID, subjectID, true);
        CompletableFuture<List<SectionNotesResponse>> snrlFuture = bannerAPIService.fetchSectionNotesAsync(instID, termID, subjectID, true);
        CompletableFuture<List<SectionAttribResponse>> sarlFuture = bannerAPIService.fetchSectionAttributesAsync(instID, termID, subjectID, true);
        // meeting details
        CompletableFuture<List<MeetingsResponse>> mrlFuture = bannerAPIService.fetchMeetingsAsync(instID, termID, subjectID, true);

        // return future of waiting for all endpoints to resolve
        return CompletableFuture.allOf(
                crlFuture, cdrlFuture, sdrlFuture, snrlFuture,
                sarlFuture, bsrlFuture, scrlFuture, mrlFuture
                // then map results to DTO
        ).thenApply(v -> new SubjectResult(
                subjectID,
                crlFuture.join(),
                cdrlFuture.join(),
                cgrlFuture.join(),
                sdrlFuture.join(),
                snrlFuture.join(),
                sarlFuture.join(),
                bsrlFuture.join(),
                scrlFuture.join(),
                mrlFuture.join()
        ));
    }

    /**
     * Construct a list of courses from Banner API
     *
     * @param result DTO containing all Banner API responses
     * @return List of courses and sections
     */
    private List<Course> constructCourses(SubjectResult result) {

        // init all course builders
        Map<String, Course.Builder> courseBuilderLookup = new HashMap<>();  // cache to ensure 1 builder per course not crn
        Map<String, Course.Builder> courseBuilderLookupByCRN = new HashMap<>();
        for (CoursesResponse cr : result.crl) {
            String courseKey = cr.formatCourseNumber();
            Course.Builder cb = courseBuilderLookup.computeIfAbsent(courseKey, k -> cr.toCourseBuilder());
            courseBuilderLookupByCRN.put(cr.ssbsectCrn1(), cb);
        }

        // add course details
        result.cdrl.forEach((cdr -> courseBuilderLookupByCRN.get(cdr.ssbsectCrn1()).setDescription(cdr.textNarrative())));
        result.cgrl.forEach((cdr -> courseBuilderLookupByCRN.get(cdr.ssbsectCrn1()).addGradingOption(cdr.toGradingOption())));

        // init all section builders
        Map<String, Section.Builder> sectionBuilderLookup = new HashMap<>();
        for (BaseSectionResponse bsr : result.bsrl) {
            sectionBuilderLookup.computeIfAbsent(bsr.ssbsectCrn(), k -> bsr.toSectionBuilder());
            // add restriction details to course
            Course.Builder cb = courseBuilderLookupByCRN.get(bsr.ssbsectCrn());
            cb.setApprovalAuthority(bsr.formatApprovalAuthority());
            cb.setMajorRestriction(bsr.hasMajorRestriction());
        }

        // add section details
        result.sdrl.forEach((cdr -> sectionBuilderLookup.get(cdr.crn()).addDescription(cdr.text())));
        result.snrl.forEach((cdr -> sectionBuilderLookup.get(cdr.crn()).addNote(cdr.textNarrative())));
        result.sarl.forEach((cdr -> sectionBuilderLookup.get(cdr.ssbsectCrn()).addAttribute(cdr.desc())));
        result.scrl.forEach((scr -> sectionBuilderLookup.get(scr.crn()).setEnrollmentCounts(scr.enrl(), scr.maxEnrl(), scr.waitCount(), scr.waitCapacity())));

        // add meeting details
        result.mrl.forEach((mr) -> {
            // add start and end dates
            Course.Builder cb = courseBuilderLookupByCRN.get(mr.ssbsectCrn());
            cb.setStartDate(mr.formatStartDate());
            cb.setEndDate(mr.formatEndDate());
            // add meetings to a section
            sectionBuilderLookup.get(mr.ssbsectCrn()).addMeetings(mr.toMeetings());
        });

        // Add sections to courses
        sectionBuilderLookup.forEach((crn, sb) -> courseBuilderLookupByCRN.get(crn).addSection(sb.build()));
        // build and return courses
        return courseBuilderLookup.values().stream()
                .map(Course.Builder::build)
                .toList();
    }

    /**
     * Fetches and constructs the course list for a single subject.
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return Future resolving to the constructed courses for this subject
     */
    private CompletableFuture<List<Course>> constructCoursesJob(String instID, String termID, String subjectID) {
        // construct course after done fetching data
        return fetchSubjectData(instID, termID, subjectID).thenApply(this::constructCourses);
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

        // todo check if request needed against cache
        Instant start = Instant.now();
        LOGGER.info(new MessageBuilder(MessageBuilder.Type.COURSE).addDetails("Constructing %s courses".formatted(subjectIDs.size())));

        // start async jobs
        List<CompletableFuture<List<Course>>> coursesFutures = subjectIDs.stream()
                .map(subjectID -> constructCoursesJob(instID, termID, subjectID))
                .toList();

        // block until all jobs have finished
        CompletableFuture.allOf(coursesFutures.toArray(new CompletableFuture[0])).join();
        LOGGER.info(new MessageBuilder(MessageBuilder.Type.COURSE)
                .addDetails("Constructed %s courses".formatted(subjectIDs.size()))
                .setDuration(start));

        // create master list once all jobs are done
        List<Course> allCourses = coursesFutures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .toList();

        // todo store results

        // apply filter if provided
        if (courseFilterRequest != null)
            allCourses = filterCourses(courseFilterRequest, allCourses);

        // convert to dto
        return detailed
                ? allCourses.stream().map(Course::toDetailedCourseDTO).toList()
                : allCourses.stream().map(Course::toSimpleCourseDTO).toList();
    }


    /**
     * Holds the 8 Banner9 API results for a single subject.
     *
     * @param subjectID Subject code
     * @param crl       List of {@link  CourseResponse}
     * @param cdrl      List of {@link CourseDescResponse}
     * @param cgrl      List of {@link CourseGradingResponse}
     * @param sdrl      List of {@link SectionDescResponse}
     * @param snrl      List of {@link SectionNotesResponse}
     * @param sarl      List of {@link SectionAttribResponse}
     * @param bsrl      List of {@link BaseSectionResponse}
     * @param scrl      List of {@link SectionCountsResponse}
     * @param mrl       List of {@link MeetingsResponse}
     */
    protected record SubjectResult(
            String subjectID,
            List<CoursesResponse> crl,
            List<CourseDescResponse> cdrl,
            List<CourseGradingResponse> cgrl,
            List<SectionDescResponse> sdrl,
            List<SectionNotesResponse> snrl,
            List<SectionAttribResponse> sarl,
            List<BaseSectionResponse> bsrl,
            List<SectionCountsResponse> scrl,
            List<MeetingsResponse> mrl
    ) {
    }
}
