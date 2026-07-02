package com.uh.rainbow.service;


import com.uh.rainbow.entities.CourseID;
import com.uh.rainbow.entities.Section;
import com.uh.rainbow.log.Logger;
import com.uh.rainbow.log.MessageBuilder;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * <b>File:</b> Scheduler.java
 * <p>
 * <b>Description:</b> Single use scheduler that generates all valid schedules given a list of courses
 *
 * @author Derek Garcia
 */
class Scheduler {

    private static final Logger LOGGER = new Logger(Scheduler.class);
    private final Map<Integer, Section> sectionByCRN;
    private final Map<CourseID, Set<Integer>> crnsByCourseID;
    // shared solution data structure
    private final ConcurrentHashMap<Integer, Set<Integer>> schedules;

    /**
     * Create new scheduler
     *
     * @param sectionByCRN   Map of sections indexed by their course reference number
     * @param crnsByCourseID Map of the course reference numbers that belong to a course index by a course ID
     */
    public Scheduler(Map<Integer, Section> sectionByCRN, Map<CourseID, Set<Integer>> crnsByCourseID) {
        this.sectionByCRN = sectionByCRN;
        this.crnsByCourseID = crnsByCourseID;
        this.schedules = new ConcurrentHashMap<>();
    }

    /**
     * Generate the starting seed values for the scheduler.
     * A seed is all other remainingCourses sections + one section of the current course
     *
     * @return List of starting seeds
     */
    private List<Seed> generateSeeds() {
        List<Seed> seedConfigs = new ArrayList<>();
        crnsByCourseID.forEach((cid, crns) -> {
            Map<CourseID, Set<Integer>> remainingCourses = crnsByCourseID.entrySet().stream()
                    // filter out the current course
                    .filter(e -> !e.getKey().equals(cid))
                    // create copy
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> new HashSet<>(e.getValue())));
            // create a new starting config (all other remainingCourses + one section of the next course)
            crns.forEach(crn -> seedConfigs.add(new Seed(new HashMap<>(remainingCourses), crn)));
        });

        return seedConfigs;
    }

    /**
     * Attempt to solve a partially completed potentialSchedule
     *
     * @param potentialSchedule Starting potentialSchedule to complete
     */
    private void solve(PotentialSchedule potentialSchedule) {
        // Add schedule if complete and no equivalent schedule found
        if (potentialSchedule.isComplete()) {
            schedules.putIfAbsent(potentialSchedule.hashCode(), potentialSchedule.getCurrentCRNs());
            return;
        }

        // solve any successors
        potentialSchedule.getSuccessors().forEach(this::solve);
    }

    /**
     * Entrypoint to recursive solver using initial values
     *
     * @return List of valid potential schedules found
     */
    public List<List<Integer>> generateSchedules() {
        int potentialPaths = crnsByCourseID.values().stream()
                .mapToInt(Set::size)
                .reduce(1, (a, b) -> a * b);
        LOGGER.info(new MessageBuilder(MessageBuilder.Type.SCHEDULE)
                .addDetails("Generating schedules")
                .addDetails("%s potential schedules".formatted(potentialPaths)));
        Instant start = Instant.now();

        // build minimal threadpool
        List<Seed> seeds = generateSeeds();
        int poolSize = Math.min(seeds.size(), Runtime.getRuntime().availableProcessors());

        // solve each starting seed - 1 thread per seed
        try (ExecutorService executor = Executors.newFixedThreadPool(poolSize)) {
            seeds.forEach(s ->
                    executor.submit(() -> solve(new PotentialSchedule(sectionByCRN, s.remainingCourses, s.startCRN))));
        }

        // report status
        MessageBuilder mb = new MessageBuilder(MessageBuilder.Type.SCHEDULE).setDuration(start);
        if (schedules.isEmpty()) {
            LOGGER.warn(mb.addDetails("No valid schedules found"));
            return new ArrayList<>();
        }
        LOGGER.info(mb.addDetails("Found %d valid schedule%s".formatted(schedules.size(), schedules.size() == 1 ? "" : "s")));

        // convert each schedule's section set to an immutable list
        return schedules.values().stream().map(List::copyOf).toList();
    }

    /**
     * Internal starting seed for a schedule
     *
     * @param remainingCourses Remaining courses to schedule
     * @param startCRN         Course reference number of next course not in remaining courses
     */
    private record Seed(HashMap<CourseID, Set<Integer>> remainingCourses, int startCRN) {
    }
}