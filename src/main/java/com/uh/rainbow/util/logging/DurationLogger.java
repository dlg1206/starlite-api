package com.uh.rainbow.util.logging;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * <b>File:</b> DurationLogger
 * <p>
 * <b>Description:</b> D
 *
 * @author Derek Garcia
 */
public class DurationLogger{
    private final org.slf4j.Logger log;

    private final LocalDateTime start = LocalDateTime.now();

    DurationLogger(org.slf4j.Logger log){
        this.log = log;
    }

    private String getDurationMessage(){
        double d = (double) Duration.between(this.start, LocalDateTime.now()).toMillis() / (double) 1000;
        return "Completed in %ss".formatted(d);
    }

    public void reportInst(int numInsts){
        this.log.info("Campuses | Found %s campus%s | %s"
                .formatted(numInsts, numInsts == 1 ? "" : "es", getDurationMessage()));
    }

    public void reportTerms(String instID, int numTerms){
        this.log.info("Terms | [ %s ] | Found %s term%s | %s"
                .formatted(instID, numTerms, numTerms == 1 ? "" : "s", getDurationMessage()));
    }
    public void reportSubjects(String instID, String termID, int numSubjects){
        this.log.info("Subjects | [ %s %s ] | Found %s subject%s | %s"
                .formatted(instID, termID, numSubjects, numSubjects == 1 ? "" : "s", getDurationMessage()));
    }

    public void reportCourses(String instID, String termID, String subjectID, int numCourses){
        this.log.info("Courses | [ %s %s %s ] | Found %s course%s | %s"
                .formatted(instID, termID, subjectID, numCourses, numCourses == 1 ? "" : "s", getDurationMessage()));
    }

    public void reportElapsed(){
        this.log.info(getDurationMessage());
    }

}