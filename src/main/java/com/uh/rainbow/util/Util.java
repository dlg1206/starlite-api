package com.uh.rainbow.util;

import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

/**
 * <b>File:</b> Util.java
 * <p>
 * <b>Description:</b> Helper util methods
 *
 * @author Derek Garcia
 */
public class Util {


    // prevent instantiation
    private Util() {
    }

    /**
     * Build a complex uri with optional params for logging
     *
     * @param campusCode Campus code to search for subjects
     * @param termCode   Term code to search for subjects
     * @param subjects   Optional list of subject codes to filter for
     * @param detailed   Include section and meeting details in response
     * @return Formatted uri
     */
    public static String buildCoursesUri(String campusCode, String termCode, Set<String> subjects, boolean detailed) {

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromPath("/campuses/%s/terms/%s/courses".formatted(campusCode, termCode));
        // add subjects if provided
        if (subjects != null && !subjects.isEmpty())
            builder.queryParam("subjects", String.join(",", subjects));
        // add detailed param if provided
        if (detailed)
            builder.queryParam("detailed", "true");
        // build
        return builder.toUriString();
    }

    /**
     * Append 's' if appropriate
     *
     * @param quantity Quantity of subject
     * @param subject  Subject to plural
     * @return Plural subject if appropriate
     */
    public static String pluralS(int quantity, String subject) {
        String quantMsg = "%d %s".formatted(quantity, subject);
        return quantity == 1 ? quantMsg : quantMsg + "s";
    }

    /**
     * Filter a list for distinct values
     *
     * @param list List of items
     * @param <T>  Type of the result returned by the call
     * @return Unique list of times
     */
    public static <T> List<T> distinct(List<T> list) {
        return list == null ? null : new ArrayList<>(new LinkedHashSet<>(list));
    }

    /**
     * Semaphore wrapper to for synchronous methods
     *
     * @param semaphore Semaphore to acquire to limit
     * @param call      Supplier representing the blocking call to execute
     * @param <T>       Type of the result returned by the call
     * @return Result of the call once a permit is acquired and the call completes
     * @throws RuntimeException if the current thread is interrupted while waiting for a permit
     */
    public static <T> T callWithSemaphore(Semaphore semaphore, Supplier<T> call) {
        try {
            semaphore.acquire();
            return call.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for permit", e);
        } finally {
            semaphore.release();
        }
    }


    /**
     * Semaphore wrapper to for async methods
     *
     * @param semaphore Semaphore to acquire to limit
     * @param call      Supplier representing the nonblocking blocking call to execute
     * @param <T>       Type of the result returned by the call
     * @return Result of the call once a permit is acquired and the call completes
     * @throws RuntimeException if the current thread is interrupted while waiting for a permit
     */
    public static <T> CompletableFuture<T> asyncCallWithSemaphore(Semaphore semaphore, Supplier<CompletableFuture<T>> call) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for permit", e);
        }

        try {
            // wait until task is complete before releasing
            return call.get()
                    .whenComplete((result, ex) -> semaphore.release());
        } catch (RuntimeException e) {
            // if call.get() itself throws synchronously before returning a future, release here
            semaphore.release();
            throw e;
        }
    }
}
