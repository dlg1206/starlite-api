package com.uh.rainbow.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.uh.rainbow.dto.IdentifierDTO;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * <b>File:</b> IdentifierResponseDTO.java
 * <p>
 * <b>Description:</b> Identifier Response DTO
 *
 * @author Derek Garcia
 */
@JsonPropertyOrder({"timestamp", "identifiers"})
public class IdentifierResponse {
    public final Date timestamp;
    public final List<IdentifierDTO> identifiers;

    /**
     * Create new Identifier Response
     *
     * @param identifiers List of identifiers
     */
    public IdentifierResponse(List<IdentifierDTO> identifiers) {
        this.timestamp = new Date();
        this.identifiers = identifiers.stream()
                .sorted(Comparator.comparing(IdentifierDTO::id))
                .toList();
    }

}
