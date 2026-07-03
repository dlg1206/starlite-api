package com.uh.rainbow.service;


import com.uh.rainbow.entities.CourseID;
import com.uh.rainbow.entities.TimeBlock;
import com.uh.rainbow.log.Logger;
import com.uh.rainbow.log.MessageBuilder;
import lombok.Getter;

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
    // custom minimum remaining values comparator
    private static final Comparator<Map.Entry<CourseID, Set<Integer>>> MRV_COMPARATOR =
            Comparator.comparingInt(e -> e.getValue().size());
    private static final Logger LOGGER = new Logger(Scheduler.class);

    private static Map<Integer, TimeBlock> sectionByCRN;
    private static Integer bufferTime;
    private final Map<CourseID, Set<Integer>> crnsByCourseID;
    // shared solution data structure
    private final ConcurrentHashMap<Integer, Set<Integer>> schedules;

    /**
     * Create new scheduler
     *
     * @param sectionByCRN   Map of sections indexed by their course reference number
     * @param crnsByCourseID Map of the course reference numbers that belong to a course index by a course ID
     * @param bufferTime     Optional minimum buffer time (in minutes) between classes
     */
    public Scheduler(Map<Integer, TimeBlock> sectionByCRN, Map<CourseID, Set<Integer>> crnsByCourseID, Integer bufferTime) {
        Scheduler.sectionByCRN = sectionByCRN;
        this.crnsByCourseID = crnsByCourseID;
        this.schedules = new ConcurrentHashMap<>();
        Scheduler.bufferTime = bufferTime;
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
                    executor.submit(() -> solve(new PotentialSchedule(s.remainingCourses, s.startCRN))));
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

    /**
     * Internal schedule object used for solving schedules
     */
    private static class PotentialSchedule {

        private final Map<CourseID, Set<Integer>> remainingCourses;
        @Getter
        private final TreeSet<Integer> currentCRNs;     // Tree set orders ints in same way - can use as ID since order !matter

        /**
         * Create a new starting potential schedule
         *
         * @param remainingCourses Remaining courses to that can potentially be included in this schedule
         * @param startCRN         Section to start with
         */
        public PotentialSchedule(Map<CourseID, Set<Integer>> remainingCourses, int startCRN) {
            this.remainingCourses = remainingCourses;
            this.currentCRNs = new TreeSet<>();
            this.currentCRNs.add(startCRN);
        }

        /**
         * Private constructor that creates a copy of another schedule that includes an additional section
         *
         * @param other   Other schedule to copy
         * @param nextCRN Next course reference number / section to add
         */
        private PotentialSchedule(PotentialSchedule other, Map<CourseID, Set<Integer>> remainingCourses, int nextCRN) {
            this.remainingCourses = remainingCourses;   // generated with trySuccessors
            this.currentCRNs = new TreeSet<>(other.currentCRNs);
            this.currentCRNs.add(nextCRN);
        }


        /**
         * Attempt to create a successor schedule to this one
         *
         * @param nextCourseID Course ID the nextCRN belongs to
         * @param nextCRN      Course reference number of section to attempt to create successor with
         * @return Optional of PotentialSchedule if valid successor
         */
        private Optional<PotentialSchedule> trySuccessor(CourseID nextCourseID, int nextCRN) {
            TimeBlock nextSection = sectionByCRN.get(nextCRN);

            // check for conflicts in current schedule - reject if conflict
            boolean nextConflictsWithExisting = currentCRNs.stream()
                    .map(sectionByCRN::get)
                    .anyMatch(s -> bufferTime == null
                            ? nextSection.conflictsWith(s)
                            : nextSection.conflictsWith(s, bufferTime));
            if (nextConflictsWithExisting)
                return Optional.empty();

            // create a copy of remaining courses without the next course
            Map<CourseID, Set<Integer>> nextCourses = remainingCourses.entrySet().stream()
                    .filter(e -> !e.getKey().equals(nextCourseID))
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> new HashSet<>(e.getValue())));
            // next section doesn't conflict existing, generate valid successors
            for (CourseID otherCourseID : nextCourses.keySet()) {
                Set<Integer> crns = nextCourses.get(otherCourseID);
                // remove all sections that conflict with the target section
                Set<Integer> conflicting = crns.stream()
                        .filter(c -> bufferTime == null
                                ? nextSection.conflictsWith(sectionByCRN.get(c))
                                : nextSection.conflictsWith(sectionByCRN.get(c), bufferTime))
                        .collect(Collectors.toSet());
                crns.removeAll(conflicting);
                // exit early if all sections conflict - this means will be missing a course
                if (crns.isEmpty()) return Optional.empty();
            }

            // Each course has at least one non-conflicting section
            return Optional.of(new PotentialSchedule(this, nextCourses, nextCRN));
        }

        /**
         * Pick the course ID with the fewest section
         * Using the course with the minimum remaining values heuristic tends to
         * hit conflicts and prune dead branches earlier than a fixed/arbitrary order.
         *
         * @return Course ID with the fewest sections in the remaining courses
         */
        private CourseID pickFewestSections() {
            return remainingCourses.entrySet().stream()
                    .min(MRV_COMPARATOR)
                    .map(Map.Entry::getKey)
                    .orElseThrow(); // safe: caller already checked remainingCourses is non-empty
        }


        /**
         * Get all the successors ( current courses + 1 new course ) for this current schedule
         *
         * @return List of valid potential successor schedules
         */
        public List<PotentialSchedule> getSuccessors() {
            List<PotentialSchedule> successors = new ArrayList<>();
            // exit early - schedule is complete
            if (remainingCourses.isEmpty())
                return successors;

            // pick a single course with the fewest sections - other branches will handle other courses
            CourseID nextCourseID = pickFewestSections();
            for (int nextCRN : remainingCourses.get(nextCourseID))
                trySuccessor(nextCourseID, nextCRN).ifPresent(successors::add);

            return successors;
        }

        /**
         * Test to see if this schedule is complete
         *
         * @return True if complete, false otherwise
         */
        public boolean isComplete() {
            // no courses left means all courses have been used
            return remainingCourses.isEmpty();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            PotentialSchedule that = (PotentialSchedule) o;
            // use order of crns as uid
            return Objects.equals(currentCRNs, that.currentCRNs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(currentCRNs);
        }
    }
}