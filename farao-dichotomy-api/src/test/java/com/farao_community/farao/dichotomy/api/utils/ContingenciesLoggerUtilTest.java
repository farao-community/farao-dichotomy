/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.utils;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.util.Objects;

/**
 * @author Vincent Bochet {@literal <vincent.bochet at rte-france.com>}
 */
class ContingenciesLoggerUtilTest {
    private static final String SENSITIVITY_FAILURE_WARNING_MESSAGE = "A sensitivity computation failure occurred after instant {} for contingencies: {}.";

    @Test
    void logSensitivityFailures() {
        // Given
        final Logger loggerMock = Mockito.mock(Logger.class);
        final String inputFile = "raoResultWithFailures.json";
        final String inputFilePath = Objects.requireNonNull(getClass().getResource(inputFile)).toString();
        // When
        ContingenciesLoggerUtil.logContingencies(inputFilePath, loggerMock);
        // Then
        Mockito.verify(loggerMock, Mockito.times(1)).warn(SENSITIVITY_FAILURE_WARNING_MESSAGE, "outage", "C1, C2, C3");
        Mockito.verify(loggerMock, Mockito.times(1)).warn(SENSITIVITY_FAILURE_WARNING_MESSAGE, "auto", "C1, C4, C5, C6");
        Mockito.verify(loggerMock, Mockito.times(1)).warn(SENSITIVITY_FAILURE_WARNING_MESSAGE, "curative", "C2, C5, C7");
    }

    @Test
    void logNoSensitivityFailures() {
        // Given
        final Logger loggerMock = Mockito.mock(Logger.class);
        final String inputFile = "raoResultNoFailure.json";
        final String inputFilePath = Objects.requireNonNull(getClass().getResource(inputFile)).toString();
        // When
        ContingenciesLoggerUtil.logContingencies(inputFilePath, loggerMock);
        // Then
        Mockito.verify(loggerMock, Mockito.never()).warn(Mockito.eq(SENSITIVITY_FAILURE_WARNING_MESSAGE), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void logNoComputationStatusMap() {
        // Given
        final Logger loggerMock = Mockito.mock(Logger.class);
        final String inputFile = "raoResultNoComputationStatusMap.json";
        final String inputFilePath = Objects.requireNonNull(getClass().getResource(inputFile)).toString();
        // When
        ContingenciesLoggerUtil.logContingencies(inputFilePath, loggerMock);
        // Then
        Mockito.verify(loggerMock, Mockito.never()).warn(Mockito.eq(SENSITIVITY_FAILURE_WARNING_MESSAGE), Mockito.anyString(), Mockito.anyString());
    }
}
