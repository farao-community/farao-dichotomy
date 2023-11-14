/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api.index;

import com.farao_community.farao.data.rao_result_api.RaoResult;
import com.farao_community.farao.dichotomy.api.RaoResultMock;
import com.farao_community.farao.dichotomy.api.results.DichotomyStepResult;
import com.farao_community.farao.dichotomy.api.results.ReasonInvalid;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
class BiDirectionalStepsIndexStrategyTest {

    private final DichotomyStepResult<RaoResult> stepResultOk = DichotomyStepResult.fromNetworkValidationResult(
        new RaoResultMock(true), null);
    private final DichotomyStepResult<RaoResult> stepResultNOk = DichotomyStepResult.fromNetworkValidationResult(
        new RaoResultMock(false), null);
    private final DichotomyStepResult<RaoResult> stepResultFailed = DichotomyStepResult.fromFailure(ReasonInvalid.VALIDATION_FAILED, "");

    @Test
    void testStartingIndex() {
        double startingIndex = 2000;

        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsIndexStrategy(startingIndex, 650);

        assertEquals(startingIndex, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterFirstInvalid() {
        double startingIndex = 2000;
        double stepSize = 650;

        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsIndexStrategy(startingIndex, stepSize);

        index.addDichotomyStepResult(new SingleValueDichotomyStep(startingIndex), stepResultNOk);

        assertEquals(startingIndex - stepSize, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterFirstFailed() {
        double startingIndex = 2000;
        double stepSize = 650;

        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsIndexStrategy(startingIndex, stepSize);

        index.addDichotomyStepResult(new SingleValueDichotomyStep(startingIndex), stepResultFailed);

        assertEquals(startingIndex - stepSize, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterInvalidCloseToMinimum() {
        double startingIndex = 2000;
        double stepSize = 650;
        double minValue = 0;

        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(minValue), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsIndexStrategy(startingIndex, stepSize);

        index.addDichotomyStepResult(new SingleValueDichotomyStep(minValue + 100), stepResultNOk);

        assertEquals(minValue, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterFailedCloseToMinimum() {
        double startingIndex = 2000;
        double stepSize = 650;
        double minValue = 0;

        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(minValue), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsIndexStrategy(startingIndex, stepSize);

        index.addDichotomyStepResult(new SingleValueDichotomyStep(minValue + 100), stepResultFailed);

        assertEquals(minValue, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterFirstValid() {
        double startingIndex = 2000;
        double stepSize = 650;

        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsIndexStrategy(startingIndex, stepSize);

        index.addDichotomyStepResult(new SingleValueDichotomyStep(startingIndex), stepResultOk);

        assertEquals(startingIndex + stepSize, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterValidCloseToMaximum() {
        double startingIndex = 2000;
        double stepSize = 650;
        double maxValue = 5000;

        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(maxValue), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsIndexStrategy(startingIndex, stepSize);

        index.addDichotomyStepResult(new SingleValueDichotomyStep(maxValue - 100), stepResultOk);

        assertEquals(maxValue, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterValidAndInvalidSteps() {
        double startingIndex = 2000;
        double stepSize = 650;

        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsIndexStrategy(startingIndex, stepSize);

        index.addDichotomyStepResult(new SingleValueDichotomyStep(2000), stepResultOk);
        index.addDichotomyStepResult(new SingleValueDichotomyStep(3000), stepResultNOk);

        assertEquals(2500, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterValidAndFailedSteps() {
        double startingIndex = 2000;
        double stepSize = 650;

        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsIndexStrategy(startingIndex, stepSize);

        index.addDichotomyStepResult(new SingleValueDichotomyStep(2000), stepResultOk);
        index.addDichotomyStepResult(new SingleValueDichotomyStep(3000), stepResultFailed);

        assertEquals(2500, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterPrecisionReached() {
        double startingIndex = 2000;
        double stepSize = 650;

        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsIndexStrategy(startingIndex, stepSize);

        index.addDichotomyStepResult(new SingleValueDichotomyStep(2000), stepResultOk);
        index.addDichotomyStepResult(new SingleValueDichotomyStep(2020), stepResultNOk);

        AssertionError e = assertThrows(AssertionError.class, () -> indexStrategy.nextValue(index));
        assertEquals("Dichotomy engine should not ask for next value if precision is reached", e.getMessage());
    }
}
