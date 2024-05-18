package com.uh.rainbow.dto.schedule;

import com.uh.rainbow.entities.Day;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>File:</b> ScheduleDTO.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public record ScheduleDTO(
        List<ScheduleMeetingDTO> tba,
        List<ScheduleMeetingDTO> sunday,
        List<ScheduleMeetingDTO> monday,
        List<ScheduleMeetingDTO> tuesday,
        List<ScheduleMeetingDTO> wednesday,
        List<ScheduleMeetingDTO> thursday,
        List<ScheduleMeetingDTO> friday,
        List<ScheduleMeetingDTO> saturday
) {

    public static class ScheduleDTOBuilder {

        private final ScheduleDTO scheduleDTO = new ScheduleDTO(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        public ScheduleDTOBuilder addScheduleMeetingDTO(Day dow, ScheduleMeetingDTO scheduleMeetingDTO) {
            switch (dow) {
                case TBA -> this.scheduleDTO.tba.add(scheduleMeetingDTO);
                case SUNDAY -> this.scheduleDTO.sunday.add(scheduleMeetingDTO);
                case MONDAY -> this.scheduleDTO.monday.add(scheduleMeetingDTO);
                case TUESDAY -> this.scheduleDTO.tuesday.add(scheduleMeetingDTO);
                case WEDNESDAY -> this.scheduleDTO.wednesday.add(scheduleMeetingDTO);
                case THURSDAY -> this.scheduleDTO.thursday.add(scheduleMeetingDTO);
                case FRIDAY -> this.scheduleDTO.friday.add(scheduleMeetingDTO);
                case SATURDAY -> this.scheduleDTO.saturday.add(scheduleMeetingDTO);
            }
            return this;
        }

        public ScheduleDTO build() {
            return this.scheduleDTO;
        }
    }


}
