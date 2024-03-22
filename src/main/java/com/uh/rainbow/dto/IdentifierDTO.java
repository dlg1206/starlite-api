package com.uh.rainbow.dto;

import java.util.*;

/**
 * <b>File:</b> IdentifierDTO.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class IdentifierDTO extends ResponseDTO{

    private final List<Map<String, String>> identifiers = new ArrayList<>();

    public IdentifierDTO() {
        super();
    }

    public void addIdentifier(String id, String name){
        Map<String, String> idObject = new LinkedHashMap<>();
        idObject.put("id", id);
        idObject.put("name", name);
        this.identifiers.add(idObject);
    }

    public List<Map<String, String>> getIdentifiers() {
        return this.identifiers;
    }
}
