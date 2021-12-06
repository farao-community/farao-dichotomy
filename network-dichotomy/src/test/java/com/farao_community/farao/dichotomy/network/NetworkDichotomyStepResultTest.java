/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
class NetworkDichotomyStepResultTest {

    @Test
    void testWithGlskLimitation() {
        NetworkDichotomyStepResult<?> result = NetworkDichotomyStepResult.fromFailure(200, ReasonInvalid.GLSK_LIMITATION, "GLSK limits");
        assertEquals(200, result.stepValue());
        assertFalse(result.isValid());
        assertEquals(ReasonInvalid.GLSK_LIMITATION, result.getReasonInvalid());
    }

    @Test
    void testWithSecureNetworkValidationResult() {
        NetworkDichotomyStepResult<?> result = NetworkDichotomyStepResult.fromNetworkValidationResult(200, new NetworkValidationResultTest<>(true));
        assertEquals(200, result.stepValue());
        assertTrue(result.isValid());
        assertEquals(ReasonInvalid.NONE, result.getReasonInvalid());
        assertTrue(result.isValid());
    }

    @Test
    void testWithUnsecureNetworkValidationResult() {
        NetworkDichotomyStepResult<?> result = NetworkDichotomyStepResult.fromNetworkValidationResult(200, new NetworkValidationResultTest<>(false));
        assertEquals(200, result.stepValue());
        assertFalse(result.isValid());
        assertEquals(ReasonInvalid.UNSECURE_AFTER_VALIDATION, result.getReasonInvalid());
        assertFalse(result.isValid());
    }
}
