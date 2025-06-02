/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

public final class ContingenciesLoggerUtil {
    private ContingenciesLoggerUtil() {
        // Util classes cannot be instantiated
    }

    record ComputationStatusMapElement(String computationStatus, String instant, String contingency) {
    }

    public static void logContingencies(String raoResultFileUrl, Logger logger) {
        if (StringUtils.isEmpty(raoResultFileUrl) || logger == null) {
            throw new IllegalArgumentException("Parameters cannot be empty");
        }

        try (final InputStream inputStream = new URI(raoResultFileUrl).toURL().openStream()) {
            final ObjectMapper objectMapper = new ObjectMapper();
            final JsonNode computationStatusMapJsonNode = objectMapper.readTree(inputStream).get("computationStatusMap");
            final List<ComputationStatusMapElement> computationStatusMapList =
                    (computationStatusMapJsonNode == null || computationStatusMapJsonNode.isNull())
                            ? List.of()
                            : objectMapper.readValue(computationStatusMapJsonNode.traverse(), new TypeReference<>() {
                                });
            computationStatusMapList.stream()
                    .filter(csme -> "FAILURE".equalsIgnoreCase(csme.computationStatus()))
                    .collect(Collectors.groupingBy(ComputationStatusMapElement::instant))
                    .forEach((instant, value) -> {
                        final String contingencies = value.stream().map(ComputationStatusMapElement::contingency).collect(Collectors.joining(", "));
                        logger.warn("A sensitivity computation failure occurred after instant {} for contingencies: {}.", instant, contingencies);
                    });
        } catch (IOException | URISyntaxException e) {
            logger.warn("An error occurred while logging sensitivity failures", e);
        }
    }
}
