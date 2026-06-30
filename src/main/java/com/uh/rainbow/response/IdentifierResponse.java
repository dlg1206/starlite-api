package com.uh.rainbow.response;

import com.uh.rainbow.dto.identifier.IdentifierDTO;

import java.util.List;

/**
 * <b>File:</b> IdentifierResponseDTO.java
 * <p>
 * <b>Description:</b> Identifier Response DTO
 *
 * @author Derek Garcia
 */
public class IdentifierResponse extends Response {

    public final List<IdentifierDTO> identifiers;

    /**
     * Create new Identifier Response using root UH url
     *
     * @param identifiers List of identifiers
     */
    public IdentifierResponse(List<IdentifierDTO> identifiers) {
        this.identifiers = identifiers;
    }

}
