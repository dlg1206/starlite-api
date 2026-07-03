package com.uh.rainbow.service;

import com.uh.rainbow.dto.identifier.IdentifierDTO;
import com.uh.rainbow.exception.InvalidCampusCodeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * <b>File:</b> CampusService.java
 * <p>
 * <b>Description:</b> Service for campus details
 *
 * @author Derek Garcia
 */
@Service
public class CampusService {

    private final Map<String, String> campusLookup;

    /**
     * Create new code service
     *
     * @param objectMapper Jackson object mapper
     * @param campusesFile File path to campuses json file
     * @throws IOException if fail to find campus json file
     */
    public CampusService(ObjectMapper objectMapper, @Value("${rainbow.data.campuses-file}") Resource campusesFile) throws IOException {
        // load json
        try (InputStream is = campusesFile.getInputStream()) {
            this.campusLookup = objectMapper.readValue(is, new TypeReference<>() {
            });
        }
    }

    /**
     * Fetch all campus codes and names
     *
     * @return List of campus codes and names
     */
    public List<IdentifierDTO> lookupCampusCodeIdentifierDTOs() {
        return campusLookup.entrySet().stream()
                .map((e) -> new IdentifierDTO(e.getKey(), e.getValue()))
                .toList();
    }

    /**
     * Validate that this campus code exists
     *
     * @param campusCode Campus code to validate
     * @return Normalized campus code (caps)
     */
    public String validateAndNormalize(String campusCode) {
        String normalized = campusCode.toUpperCase();
        // valid
        if (campusLookup.containsKey(normalized))
            return normalized;
        // invalid
        throw new InvalidCampusCodeException(campusCode);
    }

}
