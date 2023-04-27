/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.shift;

import com.farao_community.farao.dichotomy.api.NetworkValidator;
import com.farao_community.farao.dichotomy.api.exceptions.GlskLimitationException;
import com.farao_community.farao.dichotomy.api.exceptions.RaoInterruptionException;
import com.farao_community.farao.dichotomy.api.exceptions.ShiftingException;
import com.farao_community.farao.dichotomy.api.exceptions.ValidationException;
import com.farao_community.farao.dichotomy.api.results.DichotomyStepResult;
import com.powsybl.glsk.api.io.GlskDocumentImporters;
import com.powsybl.glsk.commons.ZonalData;
import com.powsybl.iidm.modification.scalable.Scalable;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
class LinearScalerTest {
    private Network network;
    private ZonalData<Scalable> zonalScalable;
    private NetworkValidator<?> networkValidator;
    private ShiftDispatcher shiftDispatcher;

    @BeforeEach
    void setUp() {
        String networkFilename = "20210901_2230_test_network.uct";
        network = Network.read(networkFilename, getClass().getResourceAsStream(networkFilename));

        String glskFilename = "20210901_2230_213_GSK_CO_CSE1.xml";
        zonalScalable = GlskDocumentImporters
                .importGlsk(Objects.requireNonNull(getClass().getResourceAsStream(glskFilename)))
                .getZonalScalable(network);

        networkValidator = Mockito.mock(NetworkValidator.class);
        shiftDispatcher = Mockito.mock(ShiftDispatcher.class);
    }

    private <I> DichotomyStepResult<I> getStepResult(boolean secure) {
        return DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(secure), null);
    }

    @Test
    void scalingNetworkValidationStrategyWithGlskLimitation() throws ShiftingException {
        Mockito.when(shiftDispatcher.dispatch(200)).thenReturn(Map.of(
                "10YCH-SWISSGRIDZ", 5000.
        ));

        LinearScaler linearScaler = new LinearScaler(zonalScalable, shiftDispatcher);
        assertThrows(GlskLimitationException.class, () -> linearScaler.shiftNetwork(200, network));
    }

    @Test
    void scalingNetworkValidationStrategyWithSecure() throws ShiftingException, ValidationException, GlskLimitationException, RaoInterruptionException {
        Mockito.when(shiftDispatcher.dispatch(200)).thenReturn(Map.of(
                "10YCH-SWISSGRIDZ", 200.
        ));
        Mockito.when(networkValidator.validateNetwork(network, null)).thenReturn(getStepResult(true));

        LinearScaler linearScaler = new LinearScaler(zonalScalable, shiftDispatcher);
        linearScaler.shiftNetwork(200, network);
        DichotomyStepResult<?> networkStepResult = networkValidator.validateNetwork(network, null);
        assertTrue(networkStepResult.isValid());
    }

    @Test
    void scalingNetworkValidationStrategyWithUnsecure() throws ShiftingException, ValidationException, GlskLimitationException, RaoInterruptionException {
        Mockito.when(shiftDispatcher.dispatch(200)).thenReturn(Map.of(
                "10YCH-SWISSGRIDZ", 200.
        ));
        Mockito.when(networkValidator.validateNetwork(network, null)).thenReturn(getStepResult(false));

        LinearScaler linearScaler = new LinearScaler(zonalScalable, shiftDispatcher);
        linearScaler.shiftNetwork(200, network);
        DichotomyStepResult<?> networkStepResult = networkValidator.validateNetwork(network, null);
        assertFalse(networkStepResult.isValid());
    }

    @Test
    void scalingNetworkValidationStrategyWithFailure() throws ShiftingException, ValidationException, GlskLimitationException, RaoInterruptionException {
        Mockito.when(shiftDispatcher.dispatch(200)).thenReturn(Map.of(
                "10YCH-SWISSGRIDZ", 200.
        ));
        Mockito.when(networkValidator.validateNetwork(network, null)).thenThrow(new ValidationException("RAO failure"));

        LinearScaler linearScaler = new LinearScaler(zonalScalable, shiftDispatcher);
        linearScaler.shiftNetwork(200, network);
        assertThrows(ValidationException.class, () -> networkValidator.validateNetwork(network, null));
    }

    @Test
    void scalingNetworkValidationStrategyWithShiftingException() throws ShiftingException {
        Mockito.when(shiftDispatcher.dispatch(200)).thenThrow(new ShiftingException("Impossible to shift"));
        LinearScaler linearScaler = new LinearScaler(zonalScalable, shiftDispatcher);
        assertThrows(ShiftingException.class, () -> linearScaler.shiftNetwork(200, network));
    }
}
