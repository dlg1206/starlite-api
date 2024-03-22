package com.uh.rainbow.services;

import com.uh.rainbow.dto.IdentifierDTO;
import com.uh.rainbow.entities.Course;
import com.uh.rainbow.entities.Day;
import com.uh.rainbow.entities.Meeting;
import com.uh.rainbow.entities.Section;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>File:</b> HTMLParser.java
 * <p>
 * <b>Description:</b> Queries UH urls and processes the resulting HTML
 *
 * @author Derek Garcia
 */
@Service
public class HTMLParserService {

    /**
     * Regex parser that extracts params from an url
     */
    private static class URLParamExtractor{

        private final Pattern regex;

        /**
         * Create new extractor
         *
         * @param regex Regex to use to extract from url
         */
        public URLParamExtractor(String regex){
            this.regex = Pattern.compile(regex);
        }

        /**
         * Use the regex to extract the param
         *
         * @param url URL to extract param from
         * @return Value of param
         */
        public String extract(String url){
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
     * @param upe URL Param extractor to use
     * @return List of extracted ids and names
     */
    private IdentifierDTO processElements(Elements elements, URLParamExtractor upe){
        IdentifierDTO ids = new IdentifierDTO();
        // Process each element
        for(Element item : elements){
            // Extract ID from url
            item = Objects.requireNonNull(item.selectFirst("a"));
            String termID = upe.extract(item.attr("href"));

            ids.addIdentifier(termID, item.text());
        }

        return ids;
    }

    /**
     * Parse the list of UH institutions
     *
     * @return List of institution ids and names
     */
    public IdentifierDTO parseInstitutions() throws IOException {
        IdentifierDTO ids = new IdentifierDTO();

        // Get list
        Document doc = Jsoup.connect(UH_ROOT).get();
        doc.select("ul.institutions").select("li").forEach(
                (item) -> ids.addIdentifier(item.className(), item.text())
        );

        return ids;
    }

    /**
     * Parse the list of available terms for an institution
     *
     * @param instID Institution ID
     * @return List of term ids and names
     */
    public IdentifierDTO parseTerms(String instID) throws IOException {

        // Get terms
        Document doc = Jsoup.connect(UH_ROOT).data("i", instID).get();
        Elements terms = doc.select("ul.terms").select("li");

        return processElements(terms, new URLParamExtractor("t=([0-9]*)"));
    }

    /**
     * Parse the list of available subjects for an institution and term
     *
     * @param instID Institution ID
     * @param termID term ID
     * @return List of subject ids and names
     */
    public IdentifierDTO parseSubjects(String instID, String termID) throws IOException {
        IdentifierDTO ids = new IdentifierDTO();

        // Get each subject col
        Document doc = Jsoup.connect(UH_ROOT)
                .data("i", instID)
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
        ids.addIdentifiers(processElements(leftSubjects, upe).getIdentifiers());
        ids.addIdentifiers(processElements(rightSubjects, upe).getIdentifiers());

        return ids;
    }

    public List<Course> parseCourses(String instID, String termID, String subjectID) throws IOException, ParseException {
        Map<String, Course> courses = new HashMap<>();

        try{
            // Get each subject col
            Document doc = Jsoup.connect(UH_ROOT)
                    .data("i", instID)
                    .data("t", termID)
                    .data("s", subjectID)
                    .get();

            Elements rows = Objects.requireNonNull(doc.selectFirst("tbody")).select("tr");

            Element row = rows.remove(0);   // prime query
            do{

                if(row.select("td").size() < 13){
                    if(rows.isEmpty()) break;
                    row = rows.remove(0);
                    continue;
                }

                // Add new course if dne
                String cid = row.select("td").get(2).text();
                if(courses.get(cid) == null)
                    courses.put(cid, new Course(
                            cid,                                     // Course ID
                            row.select("td").get(4).text(),  // Full course name
                            row.select("td").get(5).text()   // Credits
                    ));
                // Parse section
                Section section = new Section(
                        row.select("td").get(3).text(),                     // Section Number
                        Integer.parseInt(row.select("td").get(1).text()),   // Course Ref Number
                        row.select("td").get(6).select("abbr").attr("title"),   // Instructor
                        Integer.parseInt(row.select("td").get(7).text()),   // Number Enrolled
                        Integer.parseInt(row.select("td").get(8).text())    // Seats Available
                );


                int initial_offset = 0;
                // todo add wait list support
                // account for wait list rows
                // https://www.sis.hawaii.edu/uhdad/avail.classes?i=MAN&t=202440&s=THEA
                if(row.select("td").size() == 15)
                    initial_offset = 2;

                // Keep processing rows until hit next section
                do{
                    int offset = initial_offset;

                    // Different amount of columns per row can cause issues, check for offset
                    if(!Day.toDays(row.select("td").get(8 + offset).text()).isEmpty())
                        offset += -1;

                    section.addMeetings(Meeting.createMeetings(
                            row.select("td").get(9 + offset).text(),     // Day
                            row.select("td").get(10 + offset).text(),    // Times
                            row.select("td").get(11 + offset).select("abbr").attr("title"),  // Room
                            row.select("td").get(12 + offset).text()     // Dates
                    ));

                    // Add Requirements / Designation Codes / Misc info if any
                    if(!row.select("td").get(0).text().isEmpty())
                        section.addDetails(row.select("td").get(0).text());

                    if(rows.isEmpty())  break;

                    row = rows.remove(0);

                    // Edge case where details on next line but there's no times to process
                    // https://www.sis.hawaii.edu/uhdad/avail.classes?i=HAW&t=202310&s=FIRE
                    String details = row.select("td").get(0).text();
                    if(!details.isEmpty() && row.select("td").size() == 1)
                        section.addDetails(details);

                } while (!rows.isEmpty() && row.select("td").size() > 2 && row.select("td").get(1).text().isEmpty());

                // Update course
                courses.get(cid).addSection(section);
            } while (!rows.isEmpty());

        } catch (Exception ignored){
            throw ignored;
        }

        return courses.values().stream().toList();
    }
}
