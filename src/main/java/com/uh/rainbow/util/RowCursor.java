package com.uh.rainbow.util;

import com.uh.rainbow.entities.Course;
import com.uh.rainbow.entities.Day;
import com.uh.rainbow.entities.Meeting;
import com.uh.rainbow.entities.Section;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.util.List;

/**
 * <b>File:</b> RowCursor.java
 * <p>
 * <b>Description:</b> Cursor used to process UH course tables
 *
 * @author Derek Garcia
 */
public class RowCursor {

    private final Elements table;

    /**
     * Create new Row Cursor for a given table
     *
     * @param table List of rows that compose a table
     */
    public RowCursor(Elements table) {
        this.table = table;
    }

    /**
     * Peek at top row in table to see if it contains a section
     *
     * @return True if section, false otherwise
     */
    private boolean hasSection() {

        // If no rows left, then no Sections
        if (this.table.isEmpty())
            return false;

        Element row = this.table.get(0);     // peek

        // Min size for parsing meetings
        if (row.select("td").size() < 13)
            return false;

        String crn = row.select("td").get(1).text();
        if (crn.isEmpty())
            return false;

        String cid = row.select("td").get(2).text();
        if (cid.isEmpty())
            return false;

        String sid = row.select("td").get(3).text();
        if (sid.isEmpty())
            return false;

        String instructor = row.select("td").get(6).select("abbr").attr("title");
        if (instructor.isEmpty())
            return false;

        String numEnrolled = row.select("td").get(7).text();
        if (numEnrolled.isEmpty())
            return false;

        String seatsAvailable = row.select("td").get(8).text();
        return !seatsAvailable.isEmpty();

        // Row contains a section

    }

    /**
     * Peek at top row in table to see if it contains a course
     *
     * @return True if course, false otherwise
     */
    private boolean hasCourse() {
        // If no rows left, then no Courses
        if (this.table.isEmpty())
            return false;

        Element row = this.table.get(0);     // peek

        String cid = row.select("td").get(2).text();
        if (cid.isEmpty())
            return false;

        String name = row.select("td").get(4).text();
        if (name.isEmpty())
            return false;

        String credits = row.select("td").get(5).text();
        return !credits.isEmpty();

        // Row contains a section
    }

    /**
     * Peek at top row in table to see if it contains a meeting
     *
     * @return True if meeting, false otherwise
     */
    private boolean hasMeeting() {

        // If no rows left, then no meetings
        if (this.table.isEmpty())
            return false;

        Element row = this.table.get(0);     // peek

        // Min size for parsing meetings
        if (row.select("td").size() < 8)
            return false;

        int initial_offset = 0;

        // account for wait list rows
        // https://www.sis.hawaii.edu/uhdad/avail.classes?i=MAN&t=202440&s=THEA
        if (row.select("td").size() == 15)
            initial_offset = 2;
        int offset = initial_offset;

        // Different amount of columns per row can cause issues, check for offset
        if (!Day.toDays(row.select("td").get(8 + offset).text()).isEmpty())
            offset -= 1;


        String days = row.select("td").get(9 + offset).text();
        if (days.isEmpty())
            return false;

        String times = row.select("td").get(10 + offset).text();
        if (times.isEmpty())
            return false;

        String room = row.select("td").get(11 + offset).select("abbr").attr("title");
        if (room.isEmpty())
            return false;

        String dates = row.select("td").get(12 + offset).text();
        return !dates.isEmpty();

        // first row contains a Meeting
    }

    /**
     * Process rows until find a row with a meeting in it
     *
     * @return True if found meeting, false otherwise
     */
    private boolean findMeeting() {
        // Keep popping rows until find meeting
        while (!this.table.isEmpty()) {
            if (hasMeeting())
                return true;

            this.table.remove(0);     // pop
        }

        // No sections found
        return false;
    }

    /**
     * Parse the top row into a list of meetings
     *
     * @return List of Meetings
     * @throws ParseException Failed to parse meetings
     */
    private List<Meeting> getMeetings() throws ParseException {
        assert hasMeeting();    // assert meeting to process

        Element row = this.table.get(0);     // peek

        int initial_offset = 0;

        // account for wait list rows
        // https://www.sis.hawaii.edu/uhdad/avail.classes?i=MAN&t=202440&s=THEA
        if (row.select("td").size() == 15)
            initial_offset = 2;

        int offset = initial_offset;

        // Different amount of columns per row can cause issues, check for offset
        if (!Day.toDays(row.select("td").get(8 + offset).text()).isEmpty())
            offset -= 1;

        return Meeting.createMeetings(
                row.select("td").get(9 + offset).text(),     // Day
                row.select("td").get(10 + offset).text(),    // Times
                row.select("td").get(11 + offset).select("abbr").attr("title"),  // Room
                row.select("td").get(12 + offset).text()     // Dates
        );
    }

    /**
     * Process rows until find a row with a section in it
     *
     * @return True if found section, false otherwise
     */
    public boolean findSection() {
        // Keep popping rows until find section
        while (!this.table.isEmpty()) {
            if (hasSection())
                return true;

            this.table.remove(0);     // pop
        }

        // No sections found
        return false;
    }

    /**
     * Parse the top row into a section and get all meetings for that section
     *
     * @return Section
     */
    public Section getSection() {

        assert hasSection();    // assert section to process

        Element row = this.table.get(0);     // peek
        // todo add wait list support
        Section section = new Section(
                Integer.parseInt(row.select("td").get(1).text()),   // Course Ref Number
                row.select("td").get(2).text(),     // Course ID  ( ICS 101 )
                row.select("td").get(3).text(),     // Section ID ( 001 )
                row.select("td").get(6).select("abbr").attr("title"),   // Instructor
                Integer.parseInt(row.select("td").get(7).text()),   // Number Enrolled
                Integer.parseInt(row.select("td").get(8).text())    // Seats Available
        );

        // Add additional section info until reach next section
        do {
            try {
                // Add meetings
                section.addMeetings(getMeetings());
            } catch (ParseException e) {
                section.addFailedMeeting();
            }

            // Add Requirements / Designation Codes / Misc info if any
            if (!this.table.get(0).select("td").get(0).text().isEmpty())
                section.addDetails(this.table.get(0).select("td").get(0).text());
            this.table.remove(0);    // pop

            // Add details if not for the next section
            if (!(this.table.isEmpty()
                    || hasSection()
                    || this.table.get(0).select("td").get(0).text().isEmpty()))
                section.addDetails(this.table.get(0).select("td").get(0).text());

        } while (findMeeting() && !hasSection());

        return section;
    }

    /**
     * Parse the top row into a course
     *
     * @return Section
     */
    public Course getCourse() {
        assert hasCourse();
        Element row = this.table.get(0);     // peek

        return new Course(
                row.select("td").get(2).text(),  // Course ID
                row.select("td").get(4).text(),  // Full course name
                row.select("td").get(5).text()   // Credits
        );
    }

}
