package com.uh.starlite.entities;

import com.uh.starlite.dto.InstructorRecord;

/**
 * Create new instructor
 *
 * @param firstName     Instructor first name
 * @param middleInitial Instructor middle intentional
 * @param lastName      Instructor last name
 * @param username      Instructor UH username
 */
public record Instructor(String firstName, String middleInitial, String lastName, String username) {
    /**
     * Convert this instructor into a jsonl record map
     *
     * @param crn CRN of section this instructor is teaching
     * @return {@link InstructorRecord}
     */
    public InstructorRecord toInstructorRecord(int crn) {
        return new InstructorRecord(crn, username, firstName, middleInitial, lastName);
    }
}
