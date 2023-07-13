/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.index;

import com.farao_community.farao.dichotomy.api.results.DichotomyStepResult;
import com.farao_community.farao.dichotomy.api.results.RaoResultMock;
import com.farao_community.farao.dichotomy.api.results.ReasonInvalid;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel at rte-france.com>}
 * @author Vincent Bochet {@literal <vincent.bochet at rte-france.com>}
 */
class HalfRangeDivisionIndexStrategyTest {

    private static final double MIN_VALUE = 200.0;
    private static final double MAX_VALUE = 6400.0;
    private static final double PRECISION = 50.0;
    private static final DichotomyStepResult<Boolean> STEP_RESULT_SUCCESS = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), null);
    private static final DichotomyStepResult<Boolean> STEP_RESULT_FAIL = DichotomyStepResult.fromFailure(ReasonInvalid.VALIDATION_FAILED, "test");

    @Test
    void nextValueStartWithMinTest() {
        Index<Boolean> index = new Index<>(MIN_VALUE, MAX_VALUE, PRECISION);
        HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy(true);
        assertEquals(MIN_VALUE, strategy.nextValue(index), PRECISION);
        // if min is insecure the process stops otherwise we have a lowestValid
        DichotomyStepResult<Boolean> result = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), true);
        index.addDichotomyStepResult(MIN_VALUE, result);
        assertEquals((MIN_VALUE + MAX_VALUE) / 2, strategy.nextValue(index), PRECISION);
        //we now have two options either valid or not: here we test valid
        DichotomyStepResult<Boolean> result2 = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), true);
        index.addDichotomyStepResult((MIN_VALUE + MAX_VALUE) / 2, result2);
        assertEquals(((MIN_VALUE + MAX_VALUE) / 2 + MAX_VALUE) / 2, strategy.nextValue(index), PRECISION);
    }

    @Test
    void nextValueStartWithMinTest2() {
        Index<Boolean> index = new Index<>(MIN_VALUE, MAX_VALUE, PRECISION);
        HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy(true);
        assertEquals(MIN_VALUE, strategy.nextValue(index), PRECISION);
        // if min is insecure the process stops otherwise we have a lowestValid
        DichotomyStepResult<Boolean> result = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), true);
        index.addDichotomyStepResult(MIN_VALUE, result);
        assertEquals((MIN_VALUE + MAX_VALUE) / 2, strategy.nextValue(index), PRECISION);
        //we now have two options either valid or not: here we test invalid
        DichotomyStepResult<Boolean> result2 = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(false), false);
        index.addDichotomyStepResult((MIN_VALUE + MAX_VALUE) / 2, result2);
        assertEquals(((MIN_VALUE + MAX_VALUE) / 2 + MIN_VALUE) / 2, strategy.nextValue(index), PRECISION);
    }

    @Test
    void nextValueStartWithMaxTest() {
        Index<Boolean> index = new Index<>(MIN_VALUE, MAX_VALUE, PRECISION);
        HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy(false);
        assertEquals(MAX_VALUE, strategy.nextValue(index), PRECISION);
        // if max is secure the process stops otherwise we have a highesInvalid
        DichotomyStepResult<Boolean> result = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(false), false);
        index.addDichotomyStepResult(MAX_VALUE, result);
        assertEquals((MIN_VALUE + MAX_VALUE) / 2, strategy.nextValue(index), PRECISION);
        //we now have two options either valid or not: here we test valid
        DichotomyStepResult<Boolean> result2 = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), true);
        index.addDichotomyStepResult((MIN_VALUE + MAX_VALUE) / 2, result2);
        assertEquals(((MIN_VALUE + MAX_VALUE) / 2 + MAX_VALUE) / 2, strategy.nextValue(index), PRECISION);
    }

    @Test
    void nextValueStartWithMaxTest2() {
        Index<Boolean> index = new Index<>(MIN_VALUE, MAX_VALUE, PRECISION);
        HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy(false);
        assertEquals(MAX_VALUE, strategy.nextValue(index), PRECISION);
        // if  max is secure the process stops otherwise we have a highesInvalid
        DichotomyStepResult<Boolean> result = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(false), false);
        index.addDichotomyStepResult(MAX_VALUE, result);
        assertEquals((MIN_VALUE + MAX_VALUE) / 2, strategy.nextValue(index), PRECISION);
        //we now have two options either valid or not: here we test invalid
        DichotomyStepResult<Boolean> result2 = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(false), false);
        index.addDichotomyStepResult((MIN_VALUE + MAX_VALUE) / 2, result2);
        assertEquals(((MIN_VALUE + MAX_VALUE) / 2 + MIN_VALUE) / 2, strategy.nextValue(index), PRECISION);
    }

    @Test
    void nextValuePrecisionReached() {
        Index<Boolean> index = new Index<>(MIN_VALUE, MAX_VALUE, PRECISION);
        HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy(false);
        assertEquals(MAX_VALUE, strategy.nextValue(index), PRECISION);

        index.addDichotomyStepResult(MAX_VALUE, STEP_RESULT_SUCCESS);
        assertTrue(strategy.precisionReached(index));

        assertThrows(AssertionError.class, () -> strategy.nextValue(index));
    }

    @Test
    void precisionReachedHighestStep() {
        Index<Boolean> index = new Index<>(0, 160, 50);
        HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy(false);

        double nextValue = strategy.nextValue(index); // 160
        index.addDichotomyStepResult(nextValue, STEP_RESULT_SUCCESS);
        assertTrue(strategy.precisionReached(index));
    }

    @Test
    void precisionReachedLowestStep() {
        Index<Boolean> index = new Index<>(0, 160, 50);
        HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy(true);

        double nextValue = strategy.nextValue(index); // 0
        index.addDichotomyStepResult(nextValue, STEP_RESULT_FAIL);
        assertTrue(strategy.precisionReached(index));
    }

    @Test
    void precisionReachedIfAllStepsAreUnsecure() {
        Index<Boolean> index = new Index<>(0, 160, 50);
        HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy(false);

        double nextValue = strategy.nextValue(index); // 160
        index.addDichotomyStepResult(nextValue, STEP_RESULT_FAIL);
        assertFalse(strategy.precisionReached(index));

        nextValue = strategy.nextValue(index); // 80
        index.addDichotomyStepResult(nextValue, STEP_RESULT_FAIL);
        assertFalse(strategy.precisionReached(index));

        nextValue = strategy.nextValue(index); // 40
        index.addDichotomyStepResult(nextValue, STEP_RESULT_FAIL);
        assertTrue(strategy.precisionReached(index));
    }

    @Test
    void precisionReachedNoStepResult() {
        Index<Boolean> index = new Index<>(0, 160, 50);
        HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy(false);

        assertFalse(strategy.precisionReached(index));
    }

    @Test
    void precisionReached() {
        Index<Boolean> index = new Index<>(0, 220, 50);
        HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy(false);

        double nextValue = strategy.nextValue(index); // 220
        index.addDichotomyStepResult(nextValue, STEP_RESULT_FAIL);
        assertFalse(strategy.precisionReached(index));

        nextValue = strategy.nextValue(index); // 110
        index.addDichotomyStepResult(nextValue, STEP_RESULT_FAIL);
        assertFalse(strategy.precisionReached(index));

        nextValue = strategy.nextValue(index); // 55
        index.addDichotomyStepResult(nextValue, STEP_RESULT_SUCCESS);
        assertFalse(strategy.precisionReached(index));

        nextValue = strategy.nextValue(index); // 82.5
        index.addDichotomyStepResult(nextValue, STEP_RESULT_FAIL);
        assertTrue(strategy.precisionReached(index));
    }
}
