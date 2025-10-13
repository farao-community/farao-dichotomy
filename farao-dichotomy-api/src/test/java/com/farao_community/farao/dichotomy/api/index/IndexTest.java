/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.index;

import com.farao_community.farao.dichotomy.api.exceptions.DichotomyException;
import com.farao_community.farao.dichotomy.api.results.DichotomyStepResult;
import com.farao_community.farao.dichotomy.api.results.RaoResultMock;
import com.farao_community.farao.dichotomy.api.results.ReasonInvalid;
import org.junit.jupiter.api.Test;

import static com.farao_community.farao.dichotomy.api.index.Index.EPSILON;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class IndexTest {

    @Test
    void checkStandardIndexManipulation() {
        Index<?> index = new Index<>(-1000, -200, 100);

        assertEquals(-1000, index.minValue(), EPSILON);
        assertEquals(-200, index.maxValue(), EPSILON);
        assertEquals(100, index.precision(), EPSILON);
    }

    @Test
    void checkIndexCreationFailsIfMinHigherThanMax() {
        assertThrows(DichotomyException.class, () -> new Index<>(-200, -1000, 100));
    }

    @Test
    void checkIndexCreationSucceedsIfPrecisionIsLowerThanSearchInterval() {
        Index<?> index = new Index<>(0, 100, 300);
        assertEquals(0, index.minValue(), EPSILON);
        assertEquals(100, index.maxValue(), EPSILON);
        assertEquals(300, index.precision(), EPSILON);
    }

    @Test
    void checkStepsPresence() {
        Index<?> index = new Index<>(0, 1000, 100);
        //no step, nothing
        assertTrue(index.hasMissingStep());
        index.addDichotomyStepResult(700, DichotomyStepResult.fromFailure(ReasonInvalid.VALIDATION_FAILED, "test"));
        //fromFailure -> we got an invalid step, but no valid
        assertTrue(index.hasMissingStep());
        index.addDichotomyStepResult(800, DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), null));
        //then we got both
        assertFalse(index.hasMissingStep());
    }

    @Test
    void checkIfWithinPrecisionAndStepMean() {
        Index<?> index = new Index<>(0, 1000, 100);
        //difference is above 100
        index.addDichotomyStepResult(699, DichotomyStepResult.fromFailure(ReasonInvalid.VALIDATION_FAILED, "test"));
        index.addDichotomyStepResult(800, DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), null));
        assertFalse(index.isWithinPrecision());
        //difference is less than 100
        index.addDichotomyStepResult(750, DichotomyStepResult.fromFailure(ReasonInvalid.VALIDATION_FAILED, "test"));
        index.addDichotomyStepResult(830, DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), null));
        assertTrue(index.isWithinPrecision());
        //790 = (830+750)/2
        assertEquals(790, index.meanOfStepVoltages());
    }

    @Test
    void checkIfInBoundaries() {
        //
        Index<?> index = new Index<>(0, 1000, 100);
        index.addDichotomyStepResult(1e-4, DichotomyStepResult.fromFailure(ReasonInvalid.VALIDATION_FAILED, "test"));
        index.addDichotomyStepResult(999, DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), null));
        assertTrue(index.isInBounds());

        Index<?> index2 = new Index<>(0, 1000, 100);
        index2.addDichotomyStepResult(1, DichotomyStepResult.fromFailure(ReasonInvalid.VALIDATION_FAILED, "test"));
        index2.addDichotomyStepResult(999.99999999, DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), null));
        assertTrue(index2.isInBounds());

        Index<?> index3 = new Index<>(0, 1000, 100);
        index3.addDichotomyStepResult(1, DichotomyStepResult.fromFailure(ReasonInvalid.VALIDATION_FAILED, "test"));
        index3.addDichotomyStepResult(999, DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), null));
        assertFalse(index3.isInBounds());
    }
}
