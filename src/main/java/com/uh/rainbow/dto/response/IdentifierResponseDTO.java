package com.uh.rainbow.dto.response;

import com.uh.rainbow.dto.identifier.IdentifierDTO;
import com.uh.rainbow.util.SourceURLBuilder;

import java.util.List;

/**
 * <b>File:</b> IdentifierResponseDTO.java
 * <p>
 * <b>Description:</b> Identifier Response DTO
 *
 * @author Derek Garcia
 */
public class IdentifierResponseDTO extends ResponseDTO {

    public final String source;
    public final List<IdentifierDTO> identifiers;

    /**
     * Create new Identifier Response using root UH url
     *
     * @param identifiers List of identifiers
     */
    public IdentifierResponseDTO(List<IdentifierDTO> identifiers) {
        this.source = SourceURLBuilder.build();
        this.identifiers = identifiers;
    }

    /**
     * Create new Identifier Response using institution
     *
     * @param identifiers List of identifiers
     * @param instID      Campus / Institution ID
     */
    public IdentifierResponseDTO(List<IdentifierDTO> identifiers, String instID) {
        this.source = SourceURLBuilder.build(instID);
        this.identifiers = identifiers;
    }

    /**
     * Create new Identifier Response using institution and term
     *
     * @param identifiers List of identifiers
     * @param instID      Campus / Institution ID
     * @param termID      Term ID for campus
     */
    public IdentifierResponseDTO(List<IdentifierDTO> identifiers, String instID, String termID) {
        this.source = SourceURLBuilder.build(instID, termID);
        this.identifiers = identifiers;
    }
}
