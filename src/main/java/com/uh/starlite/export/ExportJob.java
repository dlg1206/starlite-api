package com.uh.starlite.export;

import com.uh.starlite.dto.ExportJobStatusDTO;
import lombok.Getter;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <b>File:</b> ExportJob.java
 * <p>
 * <b>Description:</b> Tracker for export job details
 *
 * @author Derek Garcia
 */
public class ExportJob {

    private static final ReentrantLock updateLock = new ReentrantLock();
    @Getter
    private final String uuid;
    private Status status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private AtomicInteger completed;
    private Integer total;
    private AtomicInteger failed;
    private List<ExportJobError> errors;

    /**
     * Create new job
     *
     * @param uuid UUID of job
     */
    private ExportJob(String uuid) {
        this.uuid = uuid;
        this.status = Status.NOT_STARTED;
    }

    /**
     * Accept the job
     */
    public void accept() {
        updateLock.lock();
        try {
            // todo handle starting a job that's already started
            status = Status.PENDING;
        } finally {
            updateLock.unlock();
        }
    }

    /**
     * Start the job
     *
     * @param total Number of tasks to complete
     */
    public void start(int total) {
        updateLock.lock();
        try {
            // todo handle starting a job that's already started
            status = Status.RUNNING;
            startedAt = LocalDateTime.now();
            completed = new AtomicInteger();
            this.total = total;
            failed = new AtomicInteger();
            errors = new LinkedList<>();
        } finally {
            updateLock.unlock();
        }
    }

    /**
     * Complete one task
     */
    public void completeOne() {
        completed.incrementAndGet();
    }

    /**
     * Complete the job
     */
    public void finish() {
        updateLock.lock();
        try {
            // todo handle starting a job that's already started
            finishedAt = LocalDateTime.now();
            status = Status.COMPLETED;
        } finally {
            updateLock.unlock();
        }
    }

    /**
     * Increment error count
     */
    public void error(String message) {
        updateLock.lock();
        try {
            failed.incrementAndGet();
            errors.add(new ExportJobError(message));
        } finally {
            updateLock.unlock();
        }
    }

    /**
     * Convert to job status
     *
     * @return {@link ExportJobStatusDTO}
     */
    public ExportJobStatusDTO toExportJobStatusDTO() {
        updateLock.lock();
        try {
            return new ExportJobStatusDTO(uuid, status.toString(),
                    startedAt, finishedAt,
                    completed == null ? null : completed.get(),
                    total, failed == null ? null : failed.get(),
                    errors);
        } finally {
            updateLock.unlock();
        }
    }

    /**
     * Job status
     */
    private enum Status {NOT_STARTED, PENDING, RUNNING, COMPLETED, FAILED}

    public static class Builder {
        private static final SecureRandom random = new SecureRandom();

        /**
         * Util generate uuid for jobs
         *
         * @return uuid
         */
        private static String generateUUID() {
            byte[] bytes = new byte[9]; // 9 bytes -> 12 base64url chars, no padding
            random.nextBytes(bytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        }

        /**
         * Create a new job with a unique ID
         *
         * @return {@link ExportJob}
         */
        public ExportJob newJob() {
            return new ExportJob(generateUUID());
        }


    }

}
