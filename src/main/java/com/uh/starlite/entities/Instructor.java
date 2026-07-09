package com.uh.starlite.entities;

/**
 * Create new instructor
 *
 * @param firstName     Instructor first name
 * @param middleInitial Instructor middle intentional
 * @param lastName      Instructor last name
 * @param username      Instructor UH username
 */
public record Instructor(String firstName, String middleInitial, String lastName, String username) {
}
