package com.uh.rainbow.dto.response;

import com.uh.rainbow.dto.identifier.IdentifierDTO;
import com.uh.rainbow.util.SourceURL;

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
     * @param source URL source of the identifiers
     * @param identifiers List of identifiers
     */
    public IdentifierResponseDTO(String source, List<IdentifierDTO> identifiers) {
        this.source = source;
        this.identifiers = identifiers;
    }

}
