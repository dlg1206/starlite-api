package com.uh.starlite.util;

import com.uh.starlite.dto.ScheduledCourseDTO;
import com.uh.starlite.enums.Day;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.transform.recurrence.Frequency;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;
import org.springframework.beans.factory.annotation.Value;

import java.time.*;
import java.util.*;

/**
 * <b>File:</b> IcsBuilder.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class ICSBuilder {
    // UH classes are based on Hawai'i time
    private final static ZoneId PACIFIC_HONOLULU = ZoneId.of("Pacific/Honolulu");
    private final UidGenerator uidGenerator = new RandomUidGenerator();
    @Value("${starlite.ics.prod-id}")
    private String prodID;

    /**
     * Add course meetings to calendar
     *
     * @param calendar        Calendar to add meetings to
     * @param scheduledCourse Course to get meetings for
     */
    private void addRecurringMeetings(Calendar calendar, ScheduledCourseDTO scheduledCourse) {
        // sort by class time
        Map<ClassTime, Set<Day>> meetings = new HashMap<>();
        scheduledCourse.section().meetings().forEach(m ->
                meetings.computeIfAbsent(
                        // create new index if dne
                        new ClassTime(m.startTime(), m.endTime(), m.buildingCode(), m.roomCode()),
                        k -> new HashSet<>()
                ).add(m.day())
        );

        // Shared meeting title
        String title = scheduledCourse.subjectCode() + " " + scheduledCourse.courseNumber()
                + ": " + scheduledCourse.name();

        for (Map.Entry<ClassTime, Set<Day>> entry : meetings.entrySet()) {
            // parse key-value
            ClassTime classTime = entry.getKey();
            Set<DayOfWeek> days = new HashSet<>();
            WeekDayList weekDayList = new WeekDayList();
            entry.getValue().stream()
                    // remove any null / TBD days
                    .filter(Objects::nonNull)
                    .forEach(m -> {
                        days.add(m.toDayOfWeek());
                        weekDayList.add(m.toWeekDay());
                    });

            // get the first matching day from start date
            LocalDate firstOccurrence = scheduledCourse.startDate();
            while (!days.contains(firstOccurrence.getDayOfWeek()))
                firstOccurrence = firstOccurrence.plusDays(1);

            // create zone date times
            ZonedDateTime start = ZonedDateTime.of(firstOccurrence, classTime.startTime(), PACIFIC_HONOLULU);
            ZonedDateTime end = ZonedDateTime.of(firstOccurrence, classTime.endTime(), PACIFIC_HONOLULU);
            ZonedDateTime until = ZonedDateTime.of(scheduledCourse.endDate(), classTime.endTime(), PACIFIC_HONOLULU);

            // create meeting block
            VEvent meeting = new VEvent(start, end, title);
            meeting.add(uidGenerator.generateUid());
            meeting.add(new Description(scheduledCourse.description()));
            meeting.add(new Location(classTime.building + " " + classTime.room));
            // create the repeating rule for meeting
            Recur<ZonedDateTime> recur = new Recur.Builder<ZonedDateTime>()
                    .frequency(Frequency.WEEKLY)
                    .dayList(weekDayList)
                    .until(until)
                    .build();

            // add meeting and rule
            meeting.add(new RRule<>(recur));
            calendar.add(meeting);
        }
    }

    /**
     * Create a new calendar with course meetings
     *
     * @param scheduledCourses List of courses in the schedule
     * @return Calendar with course meetings
     */
    public Calendar createICSCalendar(List<ScheduledCourseDTO> scheduledCourses) {
        Calendar calendar = new Calendar()
                .withDefaults()
                .withProdId(prodID)
                .getFluentTarget();
        // add meetings
        scheduledCourses.forEach(c -> addRecurringMeetings(calendar, c));

        return calendar;
    }

    /**
     * Meeting record stripped of day
     *
     * @param startTime Start time of class
     * @param endTime   End time of class
     */
    private record ClassTime(LocalTime startTime, LocalTime endTime, String building, String room) {
    }
}
