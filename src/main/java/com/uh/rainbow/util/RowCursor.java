package com.uh.rainbow.util;

import com.uh.rainbow.entities.Day;
import com.uh.rainbow.entities.Meeting;
import com.uh.rainbow.entities.Section;
import com.uh.rainbow.exceptions.MeetingNotFoundException;
import com.uh.rainbow.exceptions.SectionNotFoundException;
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

    private final SourceURL source;
    private final Elements table;

    /**
     * Create new Row Cursor for a given table
     *
     * @param source Source URL where the table origates
     * @param table  List of rows that compose a table
     */
    public RowCursor(SourceURL source, Elements table) {
        this.source = source;
        this.table = table;
    }

    /**
     * Peek at top row in table to see if it contains a meeting
     *
     * @return True if meeting, false otherwise
     */
    private boolean hasMeeting() {

        try {
            Element row = this.table.get(0);     // peek

            int initial_offset = 0;

            // account for wait list rows
            // https://www.sis.hawaii.edu/uhdad/avail.classes?i=MAN&t=202440&s=THEA
            if (row.select("td").size() >= 14)
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
            if (dates.isEmpty())
                return false;

        } catch (IndexOutOfBoundsException e) {
            // Row too short to parse
            return false;
        }
        return true;

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
     * @return List of meetings
     * @throws MeetingNotFoundException No meetings found in the first row
     * @throws ParseException           Failed to parse dates
     */
    private List<Meeting> getMeetings() throws MeetingNotFoundException, ParseException {
        // assert meeting to process
        if (!hasMeeting())
            throw new MeetingNotFoundException();

        Element row = this.table.get(0);     // peek

        int initial_offset = 0;

        // account for wait list rows
        // https://www.sis.hawaii.edu/uhdad/avail.classes?i=MAN&t=202440&s=THEA
        if (row.select("td").size() >= 14)
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
     * Peek at top row in table to see if it contains a section
     *
     * @return True if section, false otherwise
     */
    private boolean hasSection() {
        try {
            Element row = this.table.get(0);     // peek

            String cid = row.select("td").get(2).text();
            if (cid.isEmpty())
                return false;

            String name = row.select("td").get(4).text();
            if (name.isEmpty())
                return false;

            String credits = row.select("td").get(5).text();
            if (credits.isEmpty())
                return false;

            String crn = row.select("td").get(1).text();
            if (crn.isEmpty())
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
            if (seatsAvailable.isEmpty())
                return false;

        } catch (IndexOutOfBoundsException e) {
            // Row too short to parse
            return false;
        }
        return true;
    }

    /**
     * Process rows until find a row with a meeting in it
     *
     * @return True if found meeting, false otherwise
     */
    public boolean findSection() {
        // Keep popping rows until find meeting
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
     * @throws SectionNotFoundException First row has no section to parse
     */
    public Section getSection() throws SectionNotFoundException {
        // assert section to process
        if (!hasSection())
            throw new SectionNotFoundException();

        Element row = this.table.get(0);     // peek

        // todo add wait list support
        Section section = new Section(
                this.source,
                Integer.parseInt(row.select("td").get(1).text()),   // Course Ref Number
                row.select("td").get(2).text(),     // Course ID  ( ICS 101 )
                row.select("td").get(3).text(),     // Section ID ( 001 )
                row.select("td").get(4).text(),     // Title
                row.select("td").get(5).text(),     // Credits
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
                // failed to pase meeting
                section.addFailedMeeting();
            } catch (MeetingNotFoundException ignored) {
                // no meeting in first row
            }

            // Add Requirements / Designation Codes / Misc info if any
            if (!this.table.get(0).select("td").get(0).text().isEmpty())
                section.addDetails(this.table.get(0).select("td").get(0).text());

            this.table.remove(0);    // pop

            /*
            Next row has details for THIS section
            Example: https://www.sis.hawaii.edu/uhdad/avail.classes?i=HAW&t=202510&s=FIRE
             */
            if (!(this.table.isEmpty()
                    || hasSection()    // looking for next section to not overlap meetings
                    || this.table.get(0).select("td").get(0).text().isEmpty()))
                section.addDetails(this.table.get(0).select("td").get(0).text());

        } while (findMeeting() && !hasSection());  // looking for next section to not overlap meetings

        return section;
    }
}
