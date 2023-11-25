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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
class BiDirectionalStepsWithReferenceIndexStrategyTest {

    private final DichotomyStepResult<RaoResult> stepResultOk = DichotomyStepResult.fromNetworkValidationResult(
        new RaoResultMock(true), null);
    private final DichotomyStepResult<RaoResult> stepResultNOk = DichotomyStepResult.fromNetworkValidationResult(
        new RaoResultMock(false), null);

    private final DichotomyStepResult<RaoResult> stepResultGlskLim = DichotomyStepResult.fromFailure(ReasonInvalid.GLSK_LIMITATION, "");
    private final DichotomyStepResult<RaoResult> stepResultFailed = DichotomyStepResult.fromFailure(ReasonInvalid.VALIDATION_FAILED, "");

    @Test
    void testIndexAfterGlskLimBelowReferenceThenUnsecureBelowReference() {
        SingleDichotomyVariable startingIndex = new SingleDichotomyVariable(2000);
        double stepSize = 650;
        Index<RaoResult, SingleDichotomyVariable> index = new Index<>(new SingleDichotomyVariable(0), new SingleDichotomyVariable(5000), 50);
        IndexStrategy<SingleDichotomyVariable> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 3000);
        index.addDichotomyStepResult(startingIndex, stepResultGlskLim);
        assertEquals(2000 + 650, indexStrategy.nextValue(index).value());
        index.addDichotomyStepResult(new SingleDichotomyVariable(2650), stepResultNOk);
        assertEquals(2650 - (650. / 2), indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterGlskLimBelowReferenceThenUnsecureAboveReference() {
        SingleDichotomyVariable startingIndex = new SingleDichotomyVariable(2000);
        double stepSize = 650;
        Index<RaoResult, SingleDichotomyVariable> index = new Index<>(new SingleDichotomyVariable(0), new SingleDichotomyVariable(5000), 50);
        IndexStrategy<SingleDichotomyVariable> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 2500);
        index.addDichotomyStepResult(startingIndex, stepResultGlskLim);
        assertEquals(2000 + 650, indexStrategy.nextValue(index).value());
        index.addDichotomyStepResult(new SingleDichotomyVariable(2650), stepResultNOk);
        assertEquals(2650 - (650. / 2), indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterTwoGlskLimBelowReferenceThenGlskLimAboveReference() {
        SingleDichotomyVariable startingIndex = new SingleDichotomyVariable(2000);
        double stepSize = 650;
        Index<RaoResult, SingleDichotomyVariable> index = new Index<>(new SingleDichotomyVariable(0), new SingleDichotomyVariable(5000), 50);
        IndexStrategy<SingleDichotomyVariable> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 3000);
        index.addDichotomyStepResult(startingIndex, stepResultGlskLim);
        assertEquals(2000 + 650, indexStrategy.nextValue(index).value());
        index.addDichotomyStepResult(new SingleDichotomyVariable(2650), stepResultGlskLim);
        assertEquals(2650 + 650, indexStrategy.nextValue(index).value());
        index.addDichotomyStepResult(new SingleDichotomyVariable(3300), stepResultGlskLim);
        assertEquals(3300 - 325, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterGlskLimBelowReferenceThenSecure() {
        SingleDichotomyVariable startingIndex = new SingleDichotomyVariable(2000);
        double stepSize = 650;
        Index<RaoResult, SingleDichotomyVariable> index = new Index<>(new SingleDichotomyVariable(0), new SingleDichotomyVariable(5000), 50);
        IndexStrategy<SingleDichotomyVariable> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 3000);
        index.addDichotomyStepResult(startingIndex, stepResultGlskLim);
        assertEquals(2000 + 650, indexStrategy.nextValue(index).value());
        index.addDichotomyStepResult(new SingleDichotomyVariable(2650), stepResultOk);
        assertEquals(2650 + 650, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterGlskLimAboveReference() {
        SingleDichotomyVariable startingIndex = new SingleDichotomyVariable(2000);
        double stepSize = 650;
        Index<RaoResult, SingleDichotomyVariable> index = new Index<>(new SingleDichotomyVariable(0), new SingleDichotomyVariable(5000), 50);
        IndexStrategy<SingleDichotomyVariable> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 1500);
        index.addDichotomyStepResult(startingIndex, stepResultGlskLim);
        assertEquals(2000 - 650, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterGlskLimBelowReferenceThenUnsecureThenGlskLimThenSecure() {
        SingleDichotomyVariable startingIndex = new SingleDichotomyVariable(2000);
        double stepSize = 650;
        Index<RaoResult, SingleDichotomyVariable> index = new Index<>(new SingleDichotomyVariable(0), new SingleDichotomyVariable(5000), 50);
        IndexStrategy<SingleDichotomyVariable> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 2500);
        index.addDichotomyStepResult(startingIndex, stepResultGlskLim);
        assertEquals(2000 + 650, indexStrategy.nextValue(index).value());
        index.addDichotomyStepResult(new SingleDichotomyVariable(2650), stepResultNOk);
        assertEquals(2650 - (650. / 2), indexStrategy.nextValue(index).value());
        index.addDichotomyStepResult(new SingleDichotomyVariable(2650 - (650. / 2)), stepResultGlskLim);
        assertEquals((2650. + 2325) / 2, indexStrategy.nextValue(index).value());

        index.addDichotomyStepResult(new SingleDichotomyVariable(2487.5), stepResultOk);
        assertEquals((2650. + 2487.5) / 2, indexStrategy.nextValue(index).value());
    }

    @Test
    void testStartingIndex() {
        double startingIndex = 2000;

        Index<?, SingleDichotomyVariable> index = new Index<>(new SingleDichotomyVariable(0), new SingleDichotomyVariable(5000), 50);
        IndexStrategy<SingleDichotomyVariable> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex, 650, 2500);

        assertEquals(startingIndex, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterFirstInvalid() {
        SingleDichotomyVariable startingIndex = new SingleDichotomyVariable(2000);
        double stepSize = 650;

        Index<RaoResult, SingleDichotomyVariable> index = new Index<>(new SingleDichotomyVariable(0), new SingleDichotomyVariable(5000), 50);
        IndexStrategy<SingleDichotomyVariable> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 2500);

        index.addDichotomyStepResult(startingIndex, stepResultNOk);

        assertEquals(startingIndex.value() - stepSize, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterFirstFailed() {
        SingleDichotomyVariable startingIndex = new SingleDichotomyVariable(2000);
        double stepSize = 650;

        Index<RaoResult, SingleDichotomyVariable> index = new Index<>(new SingleDichotomyVariable(0), new SingleDichotomyVariable(5000), 50);
        IndexStrategy<SingleDichotomyVariable> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 2500);

        index.addDichotomyStepResult(startingIndex, stepResultFailed);

        assertEquals(startingIndex.value() - stepSize, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterInvalidCloseToMinimum() {
        SingleDichotomyVariable startingIndex = new SingleDichotomyVariable(2000);
        double stepSize = 650;
        double minValue = 0;

        Index<RaoResult, SingleDichotomyVariable> index = new Index<>(new SingleDichotomyVariable(minValue), new SingleDichotomyVariable(5000), 50);
        IndexStrategy<SingleDichotomyVariable> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 2500);

        index.addDichotomyStepResult(new SingleDichotomyVariable(minValue + 100), stepResultNOk);

        assertEquals(minValue, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterFailedCloseToMinimum() {
        SingleDichotomyVariable startingIndex = new SingleDichotomyVariable(2000);
        double stepSize = 650;
        double minValue = 0;

        Index<RaoResult, SingleDichotomyVariable> index = new Index<>(new SingleDichotomyVariable(minValue), new SingleDichotomyVariable(5000), 50);
        IndexStrategy<SingleDichotomyVariable> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 2500);

        index.addDichotomyStepResult(new SingleDichotomyVariable(minValue + 100), stepResultFailed);

        assertEquals(minValue, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterFirstValid() {
        SingleDichotomyVariable startingIndex = new SingleDichotomyVariable(2000);
        double stepSize = 650;

        Index<RaoResult, SingleDichotomyVariable> index = new Index<>(new SingleDichotomyVariable(0), new SingleDichotomyVariable(5000), 50);
        IndexStrategy<SingleDichotomyVariable> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 2500);

        index.addDichotomyStepResult(startingIndex, stepResultOk);

        assertEquals(startingIndex.value() + stepSize, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterValidCloseToMaximum() {
        SingleDichotomyVariable startingIndex = new SingleDichotomyVariable(2000);
        double stepSize = 650;
        double maxValue = 5000;

        Index<RaoResult, SingleDichotomyVariable> index = new Index<>(new SingleDichotomyVariable(0), new SingleDichotomyVariable(maxValue), 50);
        IndexStrategy<SingleDichotomyVariable> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 2500);

        index.addDichotomyStepResult(new SingleDichotomyVariable(maxValue - 100), stepResultOk);

        assertEquals(maxValue, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterValidAndInvalidSteps() {
        SingleDichotomyVariable startingIndex = new SingleDichotomyVariable(2000);
        double stepSize = 650;

        Index<RaoResult, SingleDichotomyVariable> index = new Index<>(new SingleDichotomyVariable(0), new SingleDichotomyVariable(5000), 50);
        IndexStrategy<SingleDichotomyVariable> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 2500);

        index.addDichotomyStepResult(new SingleDichotomyVariable(2000), stepResultOk);
        index.addDichotomyStepResult(new SingleDichotomyVariable(3000), stepResultNOk);

        assertEquals(2500, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterValidAndFailedSteps() {
        SingleDichotomyVariable startingIndex = new SingleDichotomyVariable(2000);
        double stepSize = 650;

        Index<RaoResult, SingleDichotomyVariable> index = new Index<>(new SingleDichotomyVariable(0), new SingleDichotomyVariable(5000), 50);
        IndexStrategy<SingleDichotomyVariable> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 2500);

        index.addDichotomyStepResult(new SingleDichotomyVariable(2000), stepResultOk);
        index.addDichotomyStepResult(new SingleDichotomyVariable(3000), stepResultFailed);

        assertEquals(2500, indexStrategy.nextValue(index).value());
    }
}
