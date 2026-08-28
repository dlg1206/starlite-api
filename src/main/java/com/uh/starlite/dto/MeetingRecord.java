package com.uh.starlite.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.uh.starlite.enums.Day;

import java.time.LocalTime;

/**
 * Meeting DTO with section details
 *
 * @param crn          CRN of section this meeting belongs to
 * @param day          Day of week meeting occurs
 * @param startTime    Start time of meeting in HHmm
 * @param endTime      End time of meeting in HHmm
 * @param buildingCode Building code
 * @param roomCode     Room code
 */
public record MeetingRecord(int crn,
                            Day day,
                            @JsonFormat(pattern = "HHmm") LocalTime startTime,
                            @JsonFormat(pattern = "HHmm") LocalTime endTime,
                            String buildingCode, String roomCode) {
}
