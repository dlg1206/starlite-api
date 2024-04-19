package com.uh.rainbow.service;

import com.uh.rainbow.dto.course.CourseDTO;
import com.uh.rainbow.entities.Course;
import com.uh.rainbow.entities.PotentialSchedule;
import com.uh.rainbow.entities.Section;
import org.springframework.stereotype.Service;

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

    /**
     * Generate list of valid schedules
     *
     * @param courses List of courses to include in the schedule
     * @return List of valid schedules
     */
    public List<PotentialSchedule> schedule(List<CourseDTO> courses) {
        // Convert course DTO back into courses
        List<Course> cor = new ArrayList<>();
        courses.forEach((c) -> cor.add(new Course(c.cid(), c.name(), c.credits())));

        // Convert section DTO back into sections
        List<Section> sections = new ArrayList<>();
        courses.forEach((c) -> sections.addAll(c.toSections()));        // todo add buffers

        // Generate all possible schedules
        return new Scheduler(cor).solve(new PotentialSchedule(sections));
    }

    /**
     * Scheduler that generates valid schedules
     */
    private static class Scheduler {
        private final Set<Course> requiredCourses;
        private final List<PotentialSchedule> results = new ArrayList<>();

        /**
         * Create a new Scheduler
         *
         * @param requiredCourses List of courses that must be in the final schedule
         */
        public Scheduler(List<Course> requiredCourses) {
            this.requiredCourses = new HashSet<>(requiredCourses);
        }

        /**
         * Attempt to solve a partially completed potentialSchedule
         *
         * @param potentialSchedule Starting potentialSchedule to complete
         * @return List of valid schedules
         */
        public List<PotentialSchedule> solve(PotentialSchedule potentialSchedule) {
            // Add new potentialSchedule if has all courses and the result doesn't contain an equivalent potentialSchedule
            if (potentialSchedule.isComplete(this.requiredCourses) && this.results.stream().noneMatch((s) -> s.isEquals(potentialSchedule)))
                this.results.add(potentialSchedule);

            // Solve each successor potentialSchedule
            potentialSchedule.getSuccessors().forEach(this::solve);

            // When reach here, all potential solutions have been found
            return this.results;
        }

    }

}
