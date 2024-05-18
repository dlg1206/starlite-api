package com.uh.rainbow.service;

import com.uh.rainbow.dto.identifier.IdentifierDTO;
import com.uh.rainbow.entities.Section;
import com.uh.rainbow.exception.SectionNotFoundException;
import com.uh.rainbow.util.RowCursor;
import com.uh.rainbow.util.SourceURL;
import com.uh.rainbow.util.filter.CourseFilter;
import com.uh.rainbow.util.logging.Logger;
import com.uh.rainbow.util.logging.MessageBuilder;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
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
    private static final int MAX_THREADS = 50;
    private final ForkJoinPool THREAD_POOL = new ForkJoinPool(MAX_THREADS);
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
        Instant start = Instant.now();
        // Get list
        SourceURL source = new SourceURL();
        Document doc = source.query();
        doc.select("ul.institutions").select("li").forEach(
                (item) -> identifiers.add(new IdentifierDTO(item.className(), item.text()))
        );
        LOGGER.info(new MessageBuilder(MessageBuilder.Type.INST)
                .addDetails(source.toString())
                .addDetails("Found %s campus%s".formatted(identifiers.size(), identifiers.size() == 1 ? "" : "es"))
                .setDuration(start));
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
        Instant start = Instant.now();
        // Get terms
        SourceURL source = new SourceURL(instID);
        Document doc = source.query();

        Elements terms = doc.select("ul.terms").select("li");
        List<IdentifierDTO> identifiers = extractIdentifiers(terms, new URLParamExtractor("t=([0-9]*)"));
        LOGGER.info(new MessageBuilder(MessageBuilder.Type.TERM)
                .addDetails(source)
                .addDetails("Found %s term%s".formatted(identifiers.size(), identifiers.size() == 1 ? "" : "s"))
                .setDuration(start));
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
        Instant start = Instant.now();
        // Get each subject col
        SourceURL source = new SourceURL(instID, termID);
        Document doc = source.query();

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

        LOGGER.info(new MessageBuilder(MessageBuilder.Type.SUBJECT)
                .addDetails(source)
                .addDetails("Found %s subject%s".formatted(identifiers.size(), identifiers.size() == 1 ? "" : "s"))
                .setDuration(start));

        return identifiers;
    }

    /**
     * Parse the list of available sections for an institution, term, and subject
     * Used for single subject
     *
     * @param cf        Filter to use to parse sections
     * @param instID    Institution ID
     * @param termID    term ID
     * @param subjectID subject ID
     * @return List of courses available
     * @throws IOException Fail to get html
     */
    public List<Section> parseSections(CourseFilter cf, String instID, String termID, String subjectID) throws IOException {
        Instant start = Instant.now();
        // Get each subject col
        SourceURL source = new SourceURL(instID, termID, subjectID);
        Document doc = source.query();

        // Parse all courses
        List<Section> sections = new LinkedList<>();
        RowCursor cur = new RowCursor(source, Objects.requireNonNull(doc.selectFirst("tbody")).select("tr"));
        while (cur.findSection()) {
            try {
                // Get section info
                Section section = cur.getSection();

                // Skip if invalid
                if (!cf.validSection(section))
                    continue;

                // Add valid course
                sections.add(section);

            } catch (SectionNotFoundException e) {
                LOGGER.info(new MessageBuilder(MessageBuilder.Type.COURSE).addDetails(instID, termID, subjectID).addDetails(e));
            }
        }

        LOGGER.info(new MessageBuilder(MessageBuilder.Type.COURSE)
                .addDetails(source)
                .addDetails("Found %s section%s".formatted(sections.size(), sections.size() == 1 ? "" : "s"))
                .setDuration(start));

        return sections;
    }

    /**
     * Parse the list of available sections for an institution and term
     * using the subjects permitted in the course filter
     *
     * @param cf     Filter to use to parse sections
     * @param instID Institution ID
     * @param termID term ID
     * @return List of courses available
     * @throws IOException Fail to get html
     */
    public List<Section> parseSections(CourseFilter cf, String instID, String termID) throws IOException {
        // Get all available subjects
        Instant start = Instant.now();
        List<IdentifierDTO> subjects = parseSubjects(instID, termID);

        // Parse each subject for courses
        List<CompletableFuture<List<Section>>> futures = new ArrayList<>();
        for (IdentifierDTO s : subjects) {

            // skip if not in filter
            if (!cf.validSubject(s.id()))
                continue;

            // Add async job to queue
            SourceURL source = new SourceURL(instID, termID, s.id());
            futures.add(CompletableFuture
                    .supplyAsync(() -> {
                        try {
                            // Attempt to parse
                            return parseSections(cf, instID, termID, s.id());
                        } catch (HttpStatusException e) {
                            // Report html access failure, add to failed sources and continue
                            LOGGER.warn(new MessageBuilder(MessageBuilder.Type.COURSE).addDetails("Skipping %s".formatted(source)));
                        } catch (IOException e) {
                            // Internal server error, add to failed sources and continue
                            LOGGER.error(new MessageBuilder(MessageBuilder.Type.COURSE).addDetails(e));
                        }
                        return new ArrayList<>();   // empty results
                    }, THREAD_POOL));
        }
        // Join each thread / wait for each to finish
        futures.forEach(CompletableFuture::join);

        // Get all results
        List<Section> sections = new ArrayList<>();
        for (CompletableFuture<List<Section>> result : futures) {
            try {
                sections.addAll(result.get());
            } catch (ExecutionException | InterruptedException e) {
                LOGGER.error(new MessageBuilder(MessageBuilder.Type.COURSE).addDetails(e));
            }
        }

        // Report Success and return results
        int numSites = futures.size();
        LOGGER.info(new MessageBuilder(MessageBuilder.Type.COURSE)
                .addDetails("Parsed %s site%s".formatted(numSites, numSites == 1 ? "" : "s"))
                .setDuration(start));
        return sections;
    }


}
