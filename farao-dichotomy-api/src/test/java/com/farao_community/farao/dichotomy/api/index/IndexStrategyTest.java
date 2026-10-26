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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Vincent Bochet {@literal <vincent.bochet at rte-france.com>}
 */
class IndexStrategyTest {
    private DichotomyStepResult<Boolean> stepResultSuccess;
    private IndexStrategyImpl strategy;

    private static final DichotomyStepResult<Boolean> STEP_RESULT_FAIL = DichotomyStepResult.fromFailure(ReasonInvalid.VALIDATION_FAILED, "test");

    @BeforeEach
    public void setUp() {
        Crac crac = mock(Crac.class);
        RaoParameters raoParameters = mock(RaoParameters.class);
        when(raoParameters.getNotOptimizedCnecsParameters()).thenReturn(mock(NotOptimizedCnecsParameters.class));
        stepResultSuccess = DichotomyStepResult.fromNetworkValidationResult(new RaoResultMock(true), null, crac, raoParameters);

        strategy = mock(IndexStrategyImpl.class);
        when(strategy.precisionReached(any())).thenCallRealMethod();
    }

    @Test
    void precisionReachedHighestStep() {
        Index<Boolean> index = new Index<>(0, 160, 50);
        when(strategy.nextValue(index)).thenReturn(160.);

        double nextValue = strategy.nextValue(index); // 160
        index.addDichotomyStepResult(nextValue, stepResultSuccess);
        assertTrue(strategy.precisionReached(index));
    }

    @Test
    void precisionReachedLowestStep() {
        Index<Boolean> index = new Index<>(0, 160, 50);
        when(strategy.nextValue(index)).thenReturn(0.);

        double nextValue = strategy.nextValue(index); // 0
        index.addDichotomyStepResult(nextValue, STEP_RESULT_FAIL);
        assertTrue(strategy.precisionReached(index));
    }

    @Test
    void precisionReachedIfAllStepsAreUnsecure() {
        Index<Boolean> index = new Index<>(0, 5, 1);
        when(strategy.nextValue(index)).thenReturn(5.).thenReturn(0.5).thenReturn(0.05).thenReturn(0.005).thenReturn(0.0005);

        double nextValue = strategy.nextValue(index); // 5
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
        Index<Boolean> index = new Index<>(0, 160, 50);

        assertFalse(strategy.precisionReached(index));
    }

    @Test
    void precisionReached() {
        Index<Boolean> index = new Index<>(0, 220, 50);
        when(strategy.nextValue(index)).thenReturn(220.).thenReturn(110.).thenReturn(55.).thenReturn(82.5);

        double nextValue = strategy.nextValue(index); // 220
        index.addDichotomyStepResult(nextValue, STEP_RESULT_FAIL);
        assertFalse(strategy.precisionReached(index));

        nextValue = strategy.nextValue(index); // 110
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
