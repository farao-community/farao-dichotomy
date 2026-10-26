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
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel at rte-france.com>}
 * @author Vincent Bochet {@literal <vincent.bochet at rte-france.com>}
 */
class HalfRangeDivisionIndexStrategyTest {

    private static final double MIN_VALUE = 200.0;
    private static final double MAX_VALUE = 6400.0;
    private static final double MID_VALUE = 3300.0;
    private static final double PRECISION = 50.0;

    private Crac crac;
    private RaoParameters raoParameters;
    private DichotomyStepResult<Boolean> stepResultSuccess;

    private static final DichotomyStepResult<Boolean> STEP_RESULT_FAIL = DichotomyStepResult.fromFailure(ReasonInvalid.VALIDATION_FAILED, "test");

    @BeforeEach
    public void setUp() {
        crac = mock(Crac.class);
        raoParameters = mock(RaoParameters.class);
        when(raoParameters.getNotOptimizedCnecsParameters()).thenReturn(mock(NotOptimizedCnecsParameters.class));
        stepResultSuccess = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), null, crac, raoParameters);
    }

    @Test
    void nextValueStartWithMinTest() {
        final Index<Boolean> index = new Index<>(MIN_VALUE, MAX_VALUE, PRECISION);
        final HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy<>(true);
        assertEquals(MIN_VALUE, strategy.nextValue(index), PRECISION);
        // if min is insecure the process stops otherwise we have a lowestValid
        final DichotomyStepResult<Boolean> result = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), true, crac, raoParameters);
        index.addDichotomyStepResult(MIN_VALUE, result);
        assertEquals((MIN_VALUE + MAX_VALUE) / 2, strategy.nextValue(index), PRECISION);
        //we now have two options either valid or not: here we test valid
        final DichotomyStepResult<Boolean> result2 = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), true, crac, raoParameters);
        index.addDichotomyStepResult((MIN_VALUE + MAX_VALUE) / 2, result2);
        assertEquals(((MIN_VALUE + MAX_VALUE) / 2 + MAX_VALUE) / 2, strategy.nextValue(index), PRECISION);
    }

    @Test
    void nextValueStartWithMinTest2() {
        final Index<Boolean> index = new Index<>(MIN_VALUE, MAX_VALUE, PRECISION);
        final HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy<>(true);
        assertEquals(MIN_VALUE, strategy.nextValue(index), PRECISION);
        // if min is insecure the process stops otherwise we have a lowestValid
        final DichotomyStepResult<Boolean> result = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), true, crac, raoParameters);
        index.addDichotomyStepResult(MIN_VALUE, result);
        assertEquals((MIN_VALUE + MAX_VALUE) / 2, strategy.nextValue(index), PRECISION);
        //we now have two options either valid or not: here we test invalid
        final DichotomyStepResult<Boolean> result2 = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(false), false, crac, raoParameters);
        index.addDichotomyStepResult((MIN_VALUE + MAX_VALUE) / 2, result2);
        // FIXME : assertion failed Expected : 1750.0 / Actual: 4850.0
        assertEquals(((MIN_VALUE + MAX_VALUE) / 2 + MIN_VALUE) / 2, strategy.nextValue(index), PRECISION);
    }

    @Test
    void nextValueStartWithMidSecureStepResultTest() {
        final Index<Boolean> index = new Index<>(MIN_VALUE, MAX_VALUE, PRECISION);
        final HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy<>(false);
        assertEquals(MID_VALUE, strategy.nextValue(index), PRECISION);
        //here we test mid valid
        final DichotomyStepResult<Boolean> result = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), true, crac, raoParameters);
        index.addDichotomyStepResult(MID_VALUE, result);
        assertEquals((MAX_VALUE + MID_VALUE) / 2, strategy.nextValue(index), PRECISION);
    }

    @Test
    void nextValueStartWithMidInsecureStepResultTest() {
        final Index<Boolean> index = new Index<>(MIN_VALUE, MAX_VALUE, PRECISION);
        final HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy<>(false);
        assertEquals(MID_VALUE, strategy.nextValue(index), PRECISION);
        final DichotomyStepResult<Boolean> result = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(false), false, crac, raoParameters);
        index.addDichotomyStepResult(MID_VALUE, result);
        // FIXME : assertion failed Expected : 1750.0 / Actual: 4850.0
        assertEquals((MIN_VALUE + MID_VALUE) / 2, strategy.nextValue(index), PRECISION);
    }

    @Test
    void nextValuePrecisionReached() {
        final Index<Boolean> index = new Index<>(0, 100, 50);
        final HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy<>(false);
        index.addDichotomyStepResult(50, stepResultSuccess);
        index.addDichotomyStepResult(90, STEP_RESULT_FAIL);
        assertTrue(strategy.precisionReached(index));

    }

    @Test
    void precisionReachedLowestStep() {
        final Index<Boolean> index = new Index<>(0, 160, 50);
        final HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy<>(true);

        final double nextValue = strategy.nextValue(index); // 0
        index.addDichotomyStepResult(nextValue, STEP_RESULT_FAIL);
        assertTrue(strategy.precisionReached(index));
    }

    @Test
    void precisionReachedIfAllStepsAreUnsecure() {
        final Index<Boolean> index = new Index<>(0, 160, 50);
        final HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy<>(false);

        double nextValue = strategy.nextValue(index); // 80
        index.addDichotomyStepResult(nextValue, STEP_RESULT_FAIL);
        assertFalse(strategy.precisionReached(index));

        nextValue = strategy.nextValue(index); // 40
        index.addDichotomyStepResult(nextValue, STEP_RESULT_FAIL);
        assertTrue(strategy.precisionReached(index));
    }

    @Test
    void precisionReachedNoStepResult() {
        final Index<Boolean> index = new Index<>(0, 160, 50);
        final HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy<>(false);

        assertFalse(strategy.precisionReached(index));
    }

    @Test
    void precisionReached() {
        final Index<Boolean> index = new Index<>(0, 220, 50);
        final HalfRangeDivisionIndexStrategy strategy = new HalfRangeDivisionIndexStrategy<>(false);

        double nextValue = strategy.nextValue(index); // 110
        index.addDichotomyStepResult(nextValue, STEP_RESULT_FAIL);
        assertFalse(strategy.precisionReached(index));

        nextValue = strategy.nextValue(index); // 55
        index.addDichotomyStepResult(nextValue, stepResultSuccess);
        assertFalse(strategy.precisionReached(index));

        nextValue = strategy.nextValue(index); // 82.5
        index.addDichotomyStepResult(nextValue, STEP_RESULT_FAIL);
        assertTrue(strategy.precisionReached(index));
    }
}
