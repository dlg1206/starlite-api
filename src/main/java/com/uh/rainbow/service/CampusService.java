package com.uh.rainbow.service;

import com.uh.rainbow.dto.identifier.IdentifierDTO;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <b>File:</b> CampusService.java
 * <p>
 * <b>Description:</b> Service loading University of Hawai'i campus codes
 *
 * @author Derek Garcia
 */

@Getter
@Service
public class CampusService {

    private final List<IdentifierDTO> campuses;
    private final Map<String, String> campusLookup;

    /**
     * Create new campus service
     *
     * @param objectMapper Jackson object mapper
     * @param campusesFile File path to campuses kjson file
     * @throws IOException if fail to find campus json file
     */
    public CampusService(ObjectMapper objectMapper, @Value("${rainbow.data.campuses-file}") Resource campusesFile) throws IOException {
        try (InputStream is = campusesFile.getInputStream()) {
            this.campuses = objectMapper.readValue(is, new TypeReference<>() {
            });
        }
        this.campusLookup = campuses.stream().collect(Collectors.toMap(IdentifierDTO::id, IdentifierDTO::value));
    }
}
