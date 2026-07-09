package com.uh.starlite.client.banner;

import com.uh.starlite.entities.Meeting;
import com.uh.starlite.enums.Day;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * DTO for fields from <a href="https://www.sis.hawaii.edu:9350/crseavail/api/meetings">/meetings</a> Banner9 API. Only relevant fields have been included.
 * <p>
 * Banner API returns days as keys for some reason?
 *
 * @param ssbsectCrn Course reference number
 * @param bldgCode   Building code
 * @param roomCode   Room code
 * @param startDate  Start date
 * @param endDate    End date
 * @param beginTime  Start time
 * @param endTime    End time
 * @param hrsWeek    Hours per week
 * @param sunDay     Sunday
 * @param monDay     Monday
 * @param tueDay     Tuesday
 * @param wedDay     Wednesday
 * @param thuDay     Thursday
 * @param friDay     Friday
 * @param satDay     Saturday
 */
public record MeetingsResponse(String ssbsectCrn, String bldgCode, String roomCode, String startDate, String endDate,
                               String beginTime, String endTime, int hrsWeek, String sunDay, String monDay,
                               String tueDay, String wedDay, String thuDay, String friDay,
                               String satDay) implements BannerResponse {

    /**
     * Create new meeting block
     *
     * @return One or more {@link Meeting}s
     */
    public List<Meeting> toMeetings() {
        LocalTime startTime = beginTime == null ? null : LocalTime.parse(beginTime, DateTimeFormatter.ofPattern("HHmm"));
        LocalTime endTime = this.endTime == null ? null : LocalTime.parse(this.endTime, DateTimeFormatter.ofPattern("HHmm"));
        List<String> days = Arrays.asList(sunDay, monDay, tueDay, wedDay, thuDay, friDay, satDay);

        // if no days assigned, return TBA section
        if (days.stream().allMatch(Objects::isNull)) {
            return (startTime == null && endTime == null)
                    // online async
                    ? List.of(new Meeting(null, null, null, bldgCode, roomCode))
                    // TBA section
                    : List.of(new Meeting(Day.TBD, startTime, endTime, bldgCode, roomCode));
        }

        // else get all meeting days
        return days.stream()
                .filter(Objects::nonNull)
                .map(dayCode -> new Meeting(Day.fromDayString(dayCode), startTime, endTime, bldgCode, roomCode))
                .toList();

    }

    /**
     * @return Start date as {@link LocalDate}
     */
    public LocalDate formatStartDate() {
        return LocalDate.parse(startDate);
    }

    /**
     * @return End date as {@link LocalDate}
     */
    public LocalDate formatEndDate() {
        return LocalDate.parse(endDate);
    }
}
