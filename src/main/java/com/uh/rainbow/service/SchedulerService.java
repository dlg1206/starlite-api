package com.uh.rainbow.service;

import com.uh.rainbow.entities.PotentialSchedule;
import com.uh.rainbow.entities.Section;
import com.uh.rainbow.util.logging.Logger;
import com.uh.rainbow.util.logging.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <b>File:</b> SchedulerService.java
 * <p>
 * <b>Description:</b> Service responsible for generating course schedules
 *
 * @author Derek Garcia
 */
@Service
public class SchedulerService {
    public static final Logger LOGGER = new Logger(SchedulerService.class);

    /**
     * Scheduler that generates valid schedules
     */
    private static class Scheduler {
        private final Set<String> requiredCourses = new HashSet<>();
        private final PotentialSchedule seed;
        private final List<PotentialSchedule> results = new ArrayList<>();

        /**
         * Create a new Scheduler
         *
         * @param sections Initial pool of sections to use
         */
        public Scheduler(List<Section> sections) {
            sections.forEach((s) -> this.requiredCourses.add(s.getCID()));
            this.seed = new PotentialSchedule(sections);
        }

        /**
         * Attempt to solve a partially completed potentialSchedule
         *
         * @param potentialSchedule Starting potentialSchedule to complete
         */
        private void solve(PotentialSchedule potentialSchedule) {
            // Add new potentialSchedule if has all courses and the result doesn't contain an equivalent potentialSchedule
            if (potentialSchedule.isComplete(this.requiredCourses) && this.results.stream().noneMatch((s) -> s.isEquals(potentialSchedule))) {
                LOGGER.info(new MessageBuilder(MessageBuilder.Type.SCHEDULE)
                        .addDetails("Found new schedule")
                        .addDetails(potentialSchedule)
                );
                this.results.add(potentialSchedule);
            }

            // Solve each successor potentialSchedule
            if(!potentialSchedule.getSections().isEmpty())
                LOGGER.info(new MessageBuilder(MessageBuilder.Type.SCHEDULE).addDetails("Attempting to solve " + potentialSchedule));
            potentialSchedule.getSuccessors().forEach(this::solve);

            // When reach here, all potential solutions have been found
            if(!potentialSchedule.getSections().isEmpty())
                LOGGER.info(new MessageBuilder(MessageBuilder.Type.SCHEDULE).addDetails("All schedules exhausted for " + potentialSchedule));
        }

        /**
         * Entrypoint to recursive solver using initial values
         *
         * @return List of valid potential schedules found
         */
        public List<PotentialSchedule> solve() {
            // Solve seed and return results
            Instant start = Instant.now();
            solve(this.seed);
            // Log findings
            MessageBuilder mb = new MessageBuilder(MessageBuilder.Type.SCHEDULE).setDuration(start);
            if (this.results.isEmpty()) {
                LOGGER.warn(mb.addDetails("No valid schedules found"));
            } else {
                LOGGER.info(mb.addDetails("Found %s schedule%s".formatted(this.results.size(), this.results.size() == 1 ? "" : "s")));
            }

            return this.results;
        }

    }

    /**
     * Generate list of valid schedules
     *
     * @return List of valid schedules
     */
    public List<PotentialSchedule> schedule(List<Section> sections) {
        // Generate all possible schedules
        return new Scheduler(sections).solve();
    }

}
