package com.uh.rainbow.service;

import com.uh.rainbow.banner.*;
import com.uh.rainbow.dto.course.CourseDTO;
import com.uh.rainbow.entities.Course;
import com.uh.rainbow.entities.Section;
import com.uh.rainbow.filter.CourseFilter;
import com.uh.rainbow.filter.CourseFilterMappable;
import com.uh.rainbow.response.CourseResponse;
import com.uh.rainbow.util.Timer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.uh.rainbow.util.Util.*;

/**
 * <b>File:</b> CampusService.java
 * <p>
 * <b>Description:</b> Service searching for course details
 *
 * @author Derek Garcia
 */
@Service
@RequiredArgsConstructor
public class CourseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CourseService.class);

    private final SubjectService subjectService;
    private final CourseFilterMapper courseFilterMapper;
    private final BannerAPIService bannerAPIService;

    /**
     * Fetch all course data concurrently for a single subject
     * todo refactor out into own aggregation layer (fetch from api or db)
     *
     * @param campusCode  Campus code
     * @param termCode    Term code
     * @param subjectCode Subject code
     * @return Future with subject data
     */
    @Async("bannerTaskExecutor")
    protected CompletableFuture<SubjectResult> fetchSubjectData(String campusCode, String termCode, String subjectCode) {
        // course details
        CompletableFuture<List<CoursesResponse>> crlFuture = bannerAPIService.fetchCoursesAsync(campusCode, termCode, subjectCode);
        CompletableFuture<List<CourseDescResponse>> cdrlFuture = bannerAPIService.fetchCourseDescriptionsAsync(campusCode, termCode, subjectCode);
        CompletableFuture<List<CourseGradingResponse>> cgrlFuture = bannerAPIService.fetchCourseGradingAsync(campusCode, termCode, subjectCode);
        // Section details
        CompletableFuture<List<BaseSectionResponse>> bsrlFuture = bannerAPIService.fetchSectionInstructorsAsync(campusCode, termCode, subjectCode);
        CompletableFuture<List<SectionCountsResponse>> scrlFuture = bannerAPIService.fetchSectionCountsAsync(campusCode, termCode, subjectCode);
        CompletableFuture<List<SectionDescResponse>> sdrlFuture = bannerAPIService.fetchSectionDescriptionsAsync(campusCode, termCode, subjectCode);
        CompletableFuture<List<SectionNotesResponse>> snrlFuture = bannerAPIService.fetchSectionNotesAsync(campusCode, termCode, subjectCode);
        CompletableFuture<List<SectionAttribResponse>> sarlFuture = bannerAPIService.fetchSectionAttributesAsync(campusCode, termCode, subjectCode);
        // meeting details
        CompletableFuture<List<MeetingsResponse>> mrlFuture = bannerAPIService.fetchMeetingsAsync(campusCode, termCode, subjectCode);

        // return future of waiting for all endpoints to resolve
        return CompletableFuture.allOf(
                crlFuture, cdrlFuture, cgrlFuture, sdrlFuture,
                snrlFuture, sarlFuture, bsrlFuture, scrlFuture, mrlFuture
                // then map results to DTO
        ).thenApply(v -> new SubjectResult(
                subjectCode,
                // dedupe
                distinct(crlFuture.join()),
                distinct(cdrlFuture.join()),
                distinct(cgrlFuture.join()),
                distinct(sdrlFuture.join()),
                distinct(snrlFuture.join()),
                distinct(sarlFuture.join()),
                distinct(bsrlFuture.join()),
                distinct(scrlFuture.join()),
                distinct(mrlFuture.join())
        ));
    }

    /**
     * Construct a list of courseIDs from Banner API
     *
     * @param result DTO containing all Banner API responses
     * @return List of courseIDs and sections
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

        // Add sections to courseIDs
        sectionBuilderLookup.forEach((crn, sb) -> courseBuilderLookupByCRN.get(crn).addSection(sb.build()));
        // build and return courseIDs
        return courseBuilderLookup.values().stream()
                .map(Course.Builder::build)
                .toList();
    }

    /**
     * Fetches and constructs the course list for a single subject.
     *
     * @param campusCode  Campus code
     * @param termCode    Term code
     * @param subjectCode Subject code
     * @return Future resolving to the constructed courseIDs for this subject
     */
    private CompletableFuture<List<Course>> constructCoursesJob(String campusCode, String termCode, String subjectCode) {
        // wrap batch fetch with semaphore but not construct - no limit to concurrent constructs
        Timer fetchTimer = new Timer();
        return asyncCallWithSemaphore(bannerAPIService.getBannerBatchSemaphore(),
                () -> {
                    // semaphore acquired
                    LOGGER.info("Acquired permit for {}:{}:{} after {} wait", campusCode, termCode, subjectCode, fetchTimer.formatElapsed());
                    LOGGER.info("Fetching course details for {}:{}:{}", campusCode, termCode, subjectCode);
                    fetchTimer.restart();
                    return fetchSubjectData(campusCode, termCode, subjectCode);
                }).thenApply(subjectResult -> {
            // construct courses after done fetching data
            LOGGER.info("Fetched course details for {}:{}:{} in {}", campusCode, termCode, subjectCode, fetchTimer.formatElapsed());
            LOGGER.info("Constructing {}:{}:{} courses", campusCode, termCode, subjectCode);
            Timer constructTimer = new Timer();
            List<Course> courses = constructCourses(subjectResult);
            LOGGER.info("Constructed {} {}:{}:{} courses in {}", courses.size(), campusCode, termCode, subjectCode, constructTimer.formatElapsed());
            return courses;
        });
    }

    /**
     * Apply a filter to a list of courseIDs
     *
     * @param request Request to build filter from
     * @return List of filtered courseIDs
     */
    public List<Course> filterCourses(CourseFilterMappable request, List<Course> courses) {
        CourseFilter filter = request.toCourseFilter(courseFilterMapper);
        List<Course> validCourses = new ArrayList<>();
        int sectionReject = 0;
        for (Course c : courses) {
            if (filter.rejectCourse(c)) {
                continue;
            }
            // prune invalid sections
            int before = c.getSections().size();
            c.getSections().values().removeIf(filter::rejectSection);
            sectionReject += before - c.getSections().size();
            if (!c.getSections().isEmpty())
                validCourses.add(c);
        }
        LOGGER.info("Filtered out {} and {}",
                pluralS(courses.size() - validCourses.size(), "course"),
                pluralS(sectionReject, "section"));
        return validCourses;
    }

    /**
     * Fetch all courseIDs for subjects
     *
     * @param campusCode   Campus code
     * @param termCode     Term code
     * @param subjectCodes List of subjects to fetch courseIDs for
     * @return List of courseIDs that match the filter if provided
     */
    public List<Course> fetchCourses(String campusCode, String termCode, Collection<String> subjectCodes) {
        // validate before fetch
        Set<String> normalizedSubjectCodes = subjectService.validateCampusTermSubjects(campusCode, termCode, subjectCodes);

        // todo check if request needed against cache

        // start async jobs
        LOGGER.info("Attempting to construct {}", pluralS(normalizedSubjectCodes.size(), "subject"));
        if (normalizedSubjectCodes.size() > bannerAPIService.getBatchLimit())
            LOGGER.warn("Requested subjects exceed permitted concurrent batch size ({}) - response will be slower", bannerAPIService.getBatchLimit());
        Timer timer = new Timer();
        List<CompletableFuture<List<Course>>> coursesFutures = normalizedSubjectCodes.stream()
                .map(subjectID -> constructCoursesJob(campusCode, termCode, subjectID))
                .toList();

        // block until all jobs have finished
        CompletableFuture.allOf(coursesFutures.toArray(new CompletableFuture[0])).join();
        LOGGER.info("Constructed {} in {}", pluralS(normalizedSubjectCodes.size(), "subject"), timer.formatElapsed());

        // create master list once all jobs are done
        return coursesFutures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .toList();

        // todo store results
    }


    /**
     * Fetch all courseIDs for subjects as DTOs
     *
     * @param campusCode   Campus code
     * @param termCode     Term code
     * @param subjectCodes List of subjects to fetch courseIDs for
     * @param detailed     Return detailed course information
     * @return List of courseIDs that match the filter if provided
     */
    public List<? extends CourseDTO> fetchCourseDTOs(String campusCode, String termCode, Set<String> subjectCodes, boolean detailed) {
        // wrapper for filter method
        return fetchCourseDTOs(campusCode, termCode, subjectCodes, detailed, null);
    }

    /**
     * Fetch all courseIDs for subjects as DTOs
     *
     * @param campusCode    Campus code
     * @param termCode      Term code
     * @param subjectCodes  List of subjects to fetch courseIDs for
     * @param detailed      Return detailed course information
     * @param filterRequest DTO with filter options mappable to a course filter
     * @return List of courseIDs that match the filter if provided
     */
    public List<? extends CourseDTO> fetchCourseDTOs(String campusCode, String termCode, Collection<String> subjectCodes, boolean detailed, CourseFilterMappable filterRequest) {
        // fetch courseIDs
        List<Course> allCourses = fetchCourses(campusCode, termCode, subjectCodes);

        // apply filter if provided
        if (filterRequest != null)
            allCourses = filterCourses(filterRequest, allCourses);

        if (allCourses.isEmpty())
            LOGGER.warn("No matching courses remain");

        // convert to dto
        return detailed
                ? allCourses.stream().map(Course::toDetailedCourseDTO).toList()
                : allCourses.stream().map(Course::toSimpleCourseDTO).toList();
    }

    /**
     * Holds the 9 Banner9 API results for a single subject.
     *
     * @param subjectID Subject code
     * @param crl       List of {@link CourseResponse}
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
