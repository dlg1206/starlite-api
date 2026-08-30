package com.uh.starlite.export;

import com.uh.starlite.dto.CompleteOfferingDTO;
import com.uh.starlite.dto.IdentifierDTO;

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
     * @param campuses List of campuses
     * @param data     List of complete course offerings
     * @return Export bytes
     */
    byte[] write(List<IdentifierDTO> campuses, List<CompleteOfferingDTO> data) throws IOException;
}
