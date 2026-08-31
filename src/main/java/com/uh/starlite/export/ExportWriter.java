package com.uh.starlite.export;

import com.uh.starlite.dto.CompleteOfferingDTO;

import java.io.IOException;
import java.util.List;

/**
 * <b>File:</b> ExportWriter.java
 * <p>
 * <b>Description:</b> Interface for saving data for export
 *
 * @author Derek Garcia
 */
public interface ExportWriter {

    /**
     * Close and export data
     *
     * @param data List of complete course offerings
     * @return Export bytes
     */
    byte[] write(List<CompleteOfferingDTO> data) throws IOException;
}
