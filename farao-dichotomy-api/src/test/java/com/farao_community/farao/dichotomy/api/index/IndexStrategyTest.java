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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Vincent Bochet {@literal <vincent.bochet at rte-france.com>}
 */
class IndexStrategyTest {

    private static final DichotomyStepResult<Boolean> STEP_RESULT_SUCCESS = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), null);
    private static final DichotomyStepResult<Boolean> STEP_RESULT_FAIL = DichotomyStepResult.fromFailure(ReasonInvalid.VALIDATION_FAILED, "test");

    IndexStrategyImpl strategy;

    @BeforeEach
    void setup() {
        strategy = Mockito.mock(IndexStrategyImpl.class);
        Mockito.when(strategy.precisionReached(Mockito.any())).thenCallRealMethod();
    }

    @Test
    void precisionReachedHighestStep() {
        Index<Boolean, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(160), 50);
        Mockito.when(strategy.nextValue(index)).thenReturn(new SingleValueDichotomyStep(160.));

        SingleValueDichotomyStep nextValue = strategy.nextValue(index); // 160
        index.addDichotomyStepResult(nextValue, STEP_RESULT_SUCCESS);
        assertTrue(strategy.precisionReached(index));
    }

    @Test
    void precisionReachedLowestStep() {
        Index<Boolean, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(160), 50);
        Mockito.when(strategy.nextValue(index)).thenReturn(new SingleValueDichotomyStep(0.));

        SingleValueDichotomyStep nextValue = strategy.nextValue(index); // 0
        index.addDichotomyStepResult(nextValue, STEP_RESULT_FAIL);
        assertTrue(strategy.precisionReached(index));
    }

    @Test
    void precisionReachedIfAllStepsAreUnsecure() {
        Index<Boolean, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(5), 1);
        Mockito.when(strategy.nextValue(index))
            .thenReturn(new SingleValueDichotomyStep(5))
            .thenReturn(new SingleValueDichotomyStep(0.5))
            .thenReturn(new SingleValueDichotomyStep(0.05))
            .thenReturn(new SingleValueDichotomyStep(0.005))
            .thenReturn(new SingleValueDichotomyStep(0.0005));

        SingleValueDichotomyStep nextValue = strategy.nextValue(index); // 5
        index.addDichotomyStepResult(nextValue, STEP_RESULT_FAIL);
        assertFalse(strategy.precisionReached(index));

        nextValue = strategy.nextValue(index); // 0.5
        index.addDichotomyStepResult(nextValue, STEP_RESULT_FAIL);
        assertFalse(strategy.precisionReached(index));

        nextValue = strategy.nextValue(index); // 0.05
        index.addDichotomyStepResult(nextValue, STEP_RESULT_FAIL);
        assertFalse(strategy.precisionReached(index));

        nextValue = strategy.nextValue(index); // 0.005
        index.addDichotomyStepResult(nextValue, STEP_RESULT_FAIL);
        assertFalse(strategy.precisionReached(index));

        nextValue = strategy.nextValue(index); // 0.0005
        index.addDichotomyStepResult(nextValue, STEP_RESULT_FAIL);
        assertTrue(strategy.precisionReached(index));
    }

    @Test
    void precisionReachedNoStepResult() {
        Index<Boolean, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(160), 50);

        assertFalse(strategy.precisionReached(index));
    }

    @Test
    void precisionReached() {
        Index<Boolean, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(220), 50);
        Mockito.when(strategy.nextValue(index))
            .thenReturn(new SingleValueDichotomyStep(220))
            .thenReturn(new SingleValueDichotomyStep(110))
            .thenReturn(new SingleValueDichotomyStep(55))
            .thenReturn(new SingleValueDichotomyStep(82.5));

        SingleValueDichotomyStep nextValue = strategy.nextValue(index); // 220
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
