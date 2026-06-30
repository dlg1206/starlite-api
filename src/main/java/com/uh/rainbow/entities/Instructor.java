package com.uh.rainbow.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Create new instructor
 *
 * @param firstName     Instructor first name
 * @param middleInitial Instructor middle intentional
 * @param lastName      Instructor last name
 * @param email         Instructor email address
 */
public record Instructor(String firstName, String middleInitial, String lastName, String email) {

    /**
     * @return UH ID of the instructor
     */
    @JsonIgnore
    public String getUHID() {
        return email == null ? "" : email.split("@")[0];
    }
}
