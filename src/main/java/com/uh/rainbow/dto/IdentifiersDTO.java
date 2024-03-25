package com.uh.rainbow.dto;

import com.uh.rainbow.util.SourceURLBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>File:</b> IdentifierDTO.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class IdentifiersDTO extends ResponseDTO {

    private final String source;
    private final List<IdentifierDTO> identifiers = new ArrayList<>();

    public IdentifiersDTO() {
        super();
        this.source = SourceURLBuilder.build();
    }

    public IdentifiersDTO(String instID) {
        super();
        this.source = SourceURLBuilder.build(instID.toUpperCase());
    }

    public IdentifiersDTO(String instID, String termID) {
        super();
        this.source = SourceURLBuilder.build(instID, termID);
    }

    public void addIdentifier(String id, String name) {
        this.identifiers.add(new IdentifierDTO(id, name));
    }

    public List<IdentifierDTO> getIdentifiers() {
        return this.identifiers;
    }

    public String getSource() {
        return this.source;
    }
}
