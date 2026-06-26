package com.uh.rainbow.banner;

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
                               String tueDay, String wedDay, String thuDay, String friDay, String satDay)  implements BannerResponse {
}
