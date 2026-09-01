package com.uh.starlite.entities;


import java.util.HexFormat;

import static com.uh.starlite.util.Util.getDigestInstance;

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
     * Get the checksum digest of this instructor
     *
     * @return SHA-256 digest
     */
    public String digest() {
        return HexFormat.of().formatHex(getDigestInstance(firstName, middleInitial, lastName, username).digest());
    }
}
