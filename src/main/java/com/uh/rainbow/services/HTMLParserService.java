package com.uh.rainbow.services;

import com.uh.rainbow.dto.course.CourseDTO;
import com.uh.rainbow.dto.identifier.IdentifierDTO;
import com.uh.rainbow.entities.Section;
import com.uh.rainbow.util.RowCursor;
import com.uh.rainbow.util.SourceURLBuilder;
import com.uh.rainbow.util.filter.CourseFilter;
import com.uh.rainbow.util.logging.DurationLogger;
import com.uh.rainbow.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>File:</b> HTMLParserService.java
 * <p>
 * <b>Description:</b> Queries UH urls and processes the resulting HTML
 *
 * @author Derek Garcia
 */
@Service
public class HTMLParserService {
    public static final Logger LOGGER = new Logger(HTMLParserService.class);

    /**
     * Regex parser that extracts params from an url
     */
    private static class URLParamExtractor {

        private final Pattern regex;

        /**
         * Create new extractor
         *
         * @param regex Regex to use to extract from url
         */
        public URLParamExtractor(String regex) {
            this.regex = Pattern.compile(regex);
        }

        /**
         * Use the regex to extract the param
         *
         * @param url URL to extract param from
         * @return Value of param
         */
        public String extract(String url) {
            Matcher matcher = this.regex.matcher(url);
            if (matcher.find())
                return matcher.group(1);
            return "";
        }

    }

    private static final String UH_ROOT = "https://www.sis.hawaii.edu/uhdad/avail.classes";

    /**
     * Process a list of li elements with href
     *
     * @param elements List of elements to process
     * @param upe      URL Param extractor to use
     */
    private List<IdentifierDTO> extractIdentifiers(Elements elements, URLParamExtractor upe) {
        List<IdentifierDTO> identifiers = new ArrayList<>();
        // Process each element
        while (!elements.isEmpty()) {
            Element item = elements.remove(0);  // pop
            // Extract ID from url
            item = Objects.requireNonNull(item.selectFirst("a"));
            String termID = upe.extract(item.attr("href"));
            identifiers.add(new IdentifierDTO(termID, item.text()));
        }

        return identifiers;
    }

    /**
     * Parse the list of UH institutions
     *
     * @return List of institution ids and names
     * @throws IOException Fail to get html
     */
    public List<IdentifierDTO> parseInstitutions() throws IOException {
        List<IdentifierDTO> identifiers = new ArrayList<>();
        DurationLogger dlog = LOGGER.createDurationLogger();
        // Get list
        Document doc = Jsoup.connect(UH_ROOT).get();
        doc.select("ul.institutions").select("li").forEach(
                (item) -> identifiers.add(new IdentifierDTO(item.className(), item.text()))
        );
        dlog.reportInst(identifiers.size());
        return identifiers;
    }

    /**
     * Parse the list of available terms for an institution
     *
     * @param instID Institution ID
     * @return List of term ids and names
     * @throws IOException Fail to get html
     */
    public List<IdentifierDTO> parseTerms(String instID) throws IOException {
        DurationLogger dlog = LOGGER.createDurationLogger();
        // Get terms
        Document doc = Jsoup.connect(UH_ROOT).data("i", instID.toUpperCase()).get();
        Elements terms = doc.select("ul.terms").select("li");
        List<IdentifierDTO> identifiers = extractIdentifiers(terms, new URLParamExtractor("t=([0-9]*)"));
        dlog.reportTerms(instID, identifiers.size());
        return identifiers;
    }

    /**
     * Parse the list of available subjects for an institution and term
     *
     * @param instID Institution ID
     * @param termID term ID
     * @return List of subject ids and names
     * @throws IOException Fail to get html
     */
    public List<IdentifierDTO> parseSubjects(String instID, String termID) throws IOException {
        List<IdentifierDTO> identifiers = new ArrayList<>();
        DurationLogger dlog = LOGGER.createDurationLogger();
        // Get each subject col
        Document doc = Jsoup.connect(UH_ROOT)
                .data("i", instID.toUpperCase())
                .data("t", termID)
                .get();

        Elements leftSubjects = doc
                .select("div.leftcolumn")
                .select("ul.subjects")
                .select("li");

        Elements rightSubjects = doc
                .select("div.rightcolumn")
                .select("ul.subjects")
                .select("li");

        // Process each list
        URLParamExtractor upe = new URLParamExtractor("s=(\\w*)");
        identifiers.addAll(extractIdentifiers(leftSubjects, upe));
        identifiers.addAll(extractIdentifiers(rightSubjects, upe));

        dlog.reportSubjects(instID, termID, identifiers.size());
        return identifiers;
    }

    /**
     * Parse the list of available courses for an institution, term, and subject
     *
     * @param instID    Institution ID
     * @param termID    term ID
     * @param subjectID subject ID
     * @return List of courses available
     * @throws IOException Fail to get html
     */
    public List<CourseDTO> parseCourses(CourseFilter cf, String instID, String termID, String subjectID) throws IOException {
        DurationLogger dlog = LOGGER.createDurationLogger();
        // Get each subject col
        Document doc = Jsoup.connect(UH_ROOT)
                .data("i", instID.toUpperCase())
                .data("t", termID)
                .data("s", subjectID.toUpperCase())
                .get();

        // Parse all courses
        Map<String, CourseDTO> courses = new HashMap<>();

        RowCursor cur = new RowCursor(Objects.requireNonNull(doc.selectFirst("tbody")).select("tr"));
        while (cur.findSection()) {
            try {
                // Get section info
                Section section = cur.getSection();

                // Skip if invalid
                if (!cf.validSection(section))
                    continue;

                // Add valid course
                courses.putIfAbsent(
                        section.getCourse().cid(),
                        new CourseDTO(SourceURLBuilder.build(instID, termID, subjectID), section.getCourse())
                );
                courses.get(section.getCourse().cid()).sections().add(section.toDTO());

            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage());
            }

        }
        dlog.reportCourses(instID, termID, subjectID, courses.size());
        return courses.values().stream()
                .sorted(Comparator.comparing(CourseDTO::cid))   // sort by CID
                .toList();
    }
}
