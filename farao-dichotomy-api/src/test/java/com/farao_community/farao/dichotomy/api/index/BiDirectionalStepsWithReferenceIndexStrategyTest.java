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
        SingleValueDichotomyStep startingIndex = new SingleValueDichotomyStep(2000);
        double stepSize = 650;
        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 3000);
        index.addDichotomyStepResult(startingIndex, stepResultGlskLim);
        assertEquals(2000 + 650, indexStrategy.nextValue(index).value());
        index.addDichotomyStepResult(new SingleValueDichotomyStep(2650), stepResultNOk);
        assertEquals(2650 - (650. / 2), indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterGlskLimBelowReferenceThenUnsecureAboveReference() {
        SingleValueDichotomyStep startingIndex = new SingleValueDichotomyStep(2000);
        double stepSize = 650;
        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 2500);
        index.addDichotomyStepResult(startingIndex, stepResultGlskLim);
        assertEquals(2000 + 650, indexStrategy.nextValue(index).value());
        index.addDichotomyStepResult(new SingleValueDichotomyStep(2650), stepResultNOk);
        assertEquals(2650 - (650. / 2), indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterTwoGlskLimBelowReferenceThenGlskLimAboveReference() {
        SingleValueDichotomyStep startingIndex = new SingleValueDichotomyStep(2000);
        double stepSize = 650;
        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 3000);
        index.addDichotomyStepResult(startingIndex, stepResultGlskLim);
        assertEquals(2000 + 650, indexStrategy.nextValue(index).value());
        index.addDichotomyStepResult(new SingleValueDichotomyStep(2650), stepResultGlskLim);
        assertEquals(2650 + 650, indexStrategy.nextValue(index).value());
        index.addDichotomyStepResult(new SingleValueDichotomyStep(3300), stepResultGlskLim);
        assertEquals(3300 - 325, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterGlskLimBelowReferenceThenSecure() {
        SingleValueDichotomyStep startingIndex = new SingleValueDichotomyStep(2000);
        double stepSize = 650;
        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 3000);
        index.addDichotomyStepResult(startingIndex, stepResultGlskLim);
        assertEquals(2000 + 650, indexStrategy.nextValue(index).value());
        index.addDichotomyStepResult(new SingleValueDichotomyStep(2650), stepResultOk);
        assertEquals(2650 + 650, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterGlskLimAboveReference() {
        SingleValueDichotomyStep startingIndex = new SingleValueDichotomyStep(2000);
        double stepSize = 650;
        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 1500);
        index.addDichotomyStepResult(startingIndex, stepResultGlskLim);
        assertEquals(2000 - 650, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterGlskLimBelowReferenceThenUnsecureThenGlskLimThenSecure() {
        SingleValueDichotomyStep startingIndex = new SingleValueDichotomyStep(2000);
        double stepSize = 650;
        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 2500);
        index.addDichotomyStepResult(startingIndex, stepResultGlskLim);
        assertEquals(2000 + 650, indexStrategy.nextValue(index).value());
        index.addDichotomyStepResult(new SingleValueDichotomyStep(2650), stepResultNOk);
        assertEquals(2650 - (650. / 2), indexStrategy.nextValue(index).value());
        index.addDichotomyStepResult(new SingleValueDichotomyStep(2650 - (650. / 2)), stepResultGlskLim);
        assertEquals((2650. + 2325) / 2, indexStrategy.nextValue(index).value());

        index.addDichotomyStepResult(new SingleValueDichotomyStep(2487.5), stepResultOk);
        assertEquals((2650. + 2487.5) / 2, indexStrategy.nextValue(index).value());
    }

    @Test
    void testStartingIndex() {
        double startingIndex = 2000;

        Index<?, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex, 650, 2500);

        assertEquals(startingIndex, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterFirstInvalid() {
        SingleValueDichotomyStep startingIndex = new SingleValueDichotomyStep(2000);
        double stepSize = 650;

        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 2500);

        index.addDichotomyStepResult(startingIndex, stepResultNOk);

        assertEquals(startingIndex.value() - stepSize, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterFirstFailed() {
        SingleValueDichotomyStep startingIndex = new SingleValueDichotomyStep(2000);
        double stepSize = 650;

        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 2500);

        index.addDichotomyStepResult(startingIndex, stepResultFailed);

        assertEquals(startingIndex.value() - stepSize, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterInvalidCloseToMinimum() {
        SingleValueDichotomyStep startingIndex = new SingleValueDichotomyStep(2000);
        double stepSize = 650;
        double minValue = 0;

        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(minValue), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 2500);

        index.addDichotomyStepResult(new SingleValueDichotomyStep(minValue + 100), stepResultNOk);

        assertEquals(minValue, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterFailedCloseToMinimum() {
        SingleValueDichotomyStep startingIndex = new SingleValueDichotomyStep(2000);
        double stepSize = 650;
        double minValue = 0;

        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(minValue), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 2500);

        index.addDichotomyStepResult(new SingleValueDichotomyStep(minValue + 100), stepResultFailed);

        assertEquals(minValue, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterFirstValid() {
        SingleValueDichotomyStep startingIndex = new SingleValueDichotomyStep(2000);
        double stepSize = 650;

        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 2500);

        index.addDichotomyStepResult(startingIndex, stepResultOk);

        assertEquals(startingIndex.value() + stepSize, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterValidCloseToMaximum() {
        SingleValueDichotomyStep startingIndex = new SingleValueDichotomyStep(2000);
        double stepSize = 650;
        double maxValue = 5000;

        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(maxValue), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 2500);

        index.addDichotomyStepResult(new SingleValueDichotomyStep(maxValue - 100), stepResultOk);

        assertEquals(maxValue, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterValidAndInvalidSteps() {
        SingleValueDichotomyStep startingIndex = new SingleValueDichotomyStep(2000);
        double stepSize = 650;

        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 2500);

        index.addDichotomyStepResult(new SingleValueDichotomyStep(2000), stepResultOk);
        index.addDichotomyStepResult(new SingleValueDichotomyStep(3000), stepResultNOk);

        assertEquals(2500, indexStrategy.nextValue(index).value());
    }

    @Test
    void testIndexAfterValidAndFailedSteps() {
        SingleValueDichotomyStep startingIndex = new SingleValueDichotomyStep(2000);
        double stepSize = 650;

        Index<RaoResult, SingleValueDichotomyStep> index = new Index<>(new SingleValueDichotomyStep(0), new SingleValueDichotomyStep(5000), 50);
        IndexStrategy<SingleValueDichotomyStep> indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex.value(), stepSize, 2500);

        index.addDichotomyStepResult(new SingleValueDichotomyStep(2000), stepResultOk);
        index.addDichotomyStepResult(new SingleValueDichotomyStep(3000), stepResultFailed);

        assertEquals(2500, indexStrategy.nextValue(index).value());
    }
}
