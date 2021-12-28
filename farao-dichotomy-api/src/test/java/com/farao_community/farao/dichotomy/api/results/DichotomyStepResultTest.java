/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.results;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
class DichotomyStepResultTest {

    @Test
    void testWithGlskLimitation() {
        DichotomyStepResult<?> result = DichotomyStepResult.fromFailure(ReasonInvalid.GLSK_LIMITATION, "GLSK limits");
        assertFalse(result.isValid());
        assertEquals(ReasonInvalid.GLSK_LIMITATION, result.getReasonInvalid());
    }

    @Test
    void testWithSecureNetworkValidationResult() {
        DichotomyStepResult<?> result = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), null);
        assertTrue(result.isValid());
        assertEquals(ReasonInvalid.NONE, result.getReasonInvalid());
    }

    @Test
    void testWithUnsecureNetworkValidationResult() {
        DichotomyStepResult<?> result = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(false), null);
        assertFalse(result.isValid());
        assertEquals(ReasonInvalid.UNSECURE_AFTER_VALIDATION, result.getReasonInvalid());
        assertFalse(result.isValid());
    }
}
