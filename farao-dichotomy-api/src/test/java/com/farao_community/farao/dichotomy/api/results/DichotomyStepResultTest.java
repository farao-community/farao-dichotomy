/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.results;

import com.powsybl.openrao.data.crac.api.Crac;
import com.powsybl.openrao.raoapi.parameters.NotOptimizedCnecsParameters;
import com.powsybl.openrao.raoapi.parameters.RaoParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
class DichotomyStepResultTest {

    private static RaoParameters raoParameters;
    private static Crac crac;

    @BeforeEach
    void setUp() {
        crac = mock(Crac.class);
        raoParameters = mock(RaoParameters.class);
        when(raoParameters.getNotOptimizedCnecsParameters()).thenReturn(mock(NotOptimizedCnecsParameters.class));
    }

    @Test
    void testWithGlskLimitation() {
        DichotomyStepResult<?> result = DichotomyStepResult.fromFailure(ReasonInvalid.GLSK_LIMITATION, "GLSK limits");
        assertFalse(result.isValid());
        assertEquals(ReasonInvalid.GLSK_LIMITATION, result.getReasonInvalid());
    }

    @Test
    void testWithSecureNetworkValidationResult() {
        DichotomyStepResult<?> result = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), null, crac, raoParameters);
        assertTrue(result.isValid());
        assertEquals(ReasonInvalid.NONE, result.getReasonInvalid());
    }

    @Test
    void testWithUnsecureNetworkValidationResult() {
        DichotomyStepResult<?> result = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(false), null, crac, raoParameters);
        // FIXME : assertion failed - Expected :false  Actual :true
        assertFalse(result.isValid());
        assertEquals(ReasonInvalid.UNSECURE_AFTER_VALIDATION, result.getReasonInvalid());
        assertFalse(result.isValid());
    }
}
