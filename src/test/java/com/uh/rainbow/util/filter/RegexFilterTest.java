package com.uh.rainbow.util.filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <b>File:</b> RegexFilterTest.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class RegexFilterTest {

    @Test
    public void accept_one() {
        // Given
        RegexFilter regexFilter = new RegexFilter.Builder().addString("a").build();

        // When / Then
        assertTrue(regexFilter.test("a"));
        assertFalse(regexFilter.test("c"));     // reject those not accepted
    }

    @Test
    public void accept_two() {
        // Given
        RegexFilter regexFilter = new RegexFilter.Builder().addString("a").addString("b").build();

        // When / Then
        assertTrue(regexFilter.test("a"));
        assertFalse(regexFilter.test("c"));     // reject those not accepted
    }

    @Test
    public void accept_all() {
        // Given
        RegexFilter regexFilter = new RegexFilter.Builder().build();

        // When / Then
        assertTrue(regexFilter.test("a"));
    }


    @Test
    public void reject_one() {
        // Given
        RegexFilter regexFilter = new RegexFilter.Builder().addString("!a").build();

        // When / Then
        assertFalse(regexFilter.test("a"));     // reject those in reject
        assertTrue(regexFilter.test("c"));
    }

    @Test
    public void reject_two() {
        // Given
        RegexFilter regexFilter = new RegexFilter.Builder().addString("!a").addString("!b").build();

        // When / Then
        assertFalse(regexFilter.test("a"));      // reject those in reject
        assertFalse(regexFilter.test("a"));      // reject those in reject
        assertTrue(regexFilter.test("c"));
    }

    @Test
    public void reject_one_accept_one() {
        // Given
        RegexFilter regexFilter = new RegexFilter.Builder().addString("a").addString("!b").build();

        // When / Then
        assertTrue(regexFilter.test("a"));
        assertFalse(regexFilter.test("b"));
        assertFalse(regexFilter.test("c"));     // reject those that aren't in the accept
    }

}
