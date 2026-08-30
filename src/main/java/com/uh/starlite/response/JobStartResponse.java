package com.uh.starlite.response;


import static com.uh.starlite.util.Uri.exportStatus;

/**
 * Response that job was created successfully
 *
 * @param jobID   ID of started job
 * @param pollURL Poll url for job updates
 */
public record JobStartResponse(String jobID, String pollURL) {
    /**
     * Response that job was created successfully
     *
     * @param jobID ID of started job
     */
    public JobStartResponse(String jobID) {
        this(jobID, exportStatus(jobID));
    }
}
