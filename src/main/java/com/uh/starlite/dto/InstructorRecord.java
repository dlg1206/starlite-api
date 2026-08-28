package com.uh.starlite.dto;

/**
 * Create new instructor
 *
 * @param crn                CRN of section
 *                           this instructor is teaching
 * @param instructorUsername Instructor UH username
 * @param firstName          Instructor first name
 * @param middleInitial      Instructor middle intentional
 * @param lastName           Instructor last name
 */
public record InstructorRecord(int crn, String instructorUsername, String firstName, String middleInitial,
                               String lastName) {
}
