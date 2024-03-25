package com.uh.rainbow.services;

import com.uh.rainbow.dto.CourseDTO;
import com.uh.rainbow.dto.IdentifiersDTO;
import com.uh.rainbow.entities.Course;
import com.uh.rainbow.entities.Section;
import com.uh.rainbow.util.RowCursor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    // Spring-configured logger
    public static final Logger LOGGER = LoggerFactory.getLogger(HTMLParserService.class);

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
     * @param dto      Data Transfer Object to update
     * @param elements List of elements to process
     * @param upe      URL Param extractor to use
     */
    private void updateDTO(IdentifiersDTO dto, Elements elements, URLParamExtractor upe) {
        // Process each element
        for (Element item : elements) {
            // Extract ID from url
            item = Objects.requireNonNull(item.selectFirst("a"));
            String termID = upe.extract(item.attr("href"));
            // Update DTO
            dto.addIdentifier(termID, item.text());
        }
    }

    /**
     * Parse the list of UH institutions
     *
     * @return List of institution ids and names
     * @throws IOException Fail to get html
     */
    public IdentifiersDTO parseInstitutions() throws IOException {
        IdentifiersDTO dto = new IdentifiersDTO();

        // Get list
        Document doc = Jsoup.connect(UH_ROOT).get();
        doc.select("ul.institutions").select("li").forEach(
                (item) -> dto.addIdentifier(item.className(), item.text())
        );

        return dto;
    }

    /**
     * Parse the list of available terms for an institution
     *
     * @param instID Institution ID
     * @return List of term ids and names
     * @throws IOException Fail to get html
     */
    public IdentifiersDTO parseTerms(String instID) throws IOException {
        IdentifiersDTO dto = new IdentifiersDTO(instID);

        // Get terms
        Document doc = Jsoup.connect(UH_ROOT).data("i", instID.toUpperCase()).get();
        Elements terms = doc.select("ul.terms").select("li");
        updateDTO(dto, terms, new URLParamExtractor("t=([0-9]*)"));

        return dto;
    }

    /**
     * Parse the list of available subjects for an institution and term
     *
     * @param instID Institution ID
     * @param termID term ID
     * @return List of subject ids and names
     * @throws IOException Fail to get html
     */
    public IdentifiersDTO parseSubjects(String instID, String termID) throws IOException {
        IdentifiersDTO dto = new IdentifiersDTO(instID.toUpperCase(), termID);

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
        updateDTO(dto, leftSubjects, upe);
        updateDTO(dto, rightSubjects, upe);

        return dto;
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
    public List<CourseDTO> parseCourses(String instID, String termID, String subjectID) throws IOException {

        // Get each subject col
        Document doc = Jsoup.connect(UH_ROOT)
                .data("i", instID.toUpperCase())
                .data("t", termID)
                .data("s", subjectID.toUpperCase())
                .get();

        // Parse all courses
        Map<String, Course> courses = new HashMap<>();
        RowCursor cur = new RowCursor(Objects.requireNonNull(doc.selectFirst("tbody")).select("tr"));
        while (cur.findSection()) {
            // Get course info, each section row will have course info
            Course c = cur.getCourse();
            courses.putIfAbsent(c.getCID(), c);

            // Get all remaining section info
            Section section = cur.getSection();
            courses.get(section.getcid()).addSection(section);
        }

        // Return DTOs
        ArrayList<CourseDTO> dtos = new ArrayList<>();
        courses.values().stream().toList().forEach((c) -> dtos.add(c.toCourseDTO(instID, termID, subjectID)));
        return dtos;
    }
}
