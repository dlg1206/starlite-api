package com.uh.starlite.entities;

import java.util.HexFormat;

import static com.uh.starlite.util.Util.getDigestInstance;

/**
 * Grading option for a course
 *
 * @param code        Banner grading code
 * @param description Description of grading option
 */
public record GradingOption(String code, String description) {
    /**
     * Get the checksum digest of this grading option
     *
     * @return SHA-256 digest
     */
    public String digest() {
        return HexFormat.of().formatHex(getDigestInstance(code, description).digest());
    }
}
