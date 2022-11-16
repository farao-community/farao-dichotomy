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

    @Test
    void testIndexAfterFirstInvalid() {
        double startingIndex = 2000;
        double stepSize = 650;

        Index<RaoResult> index = new Index<>(0, 5000, 50);
        IndexStrategy indexStrategy = new BiDirectionalStepsIndexStrategy(startingIndex, stepSize);

        index.addDichotomyStepResult(startingIndex, stepResultNOk);

        assertEquals(startingIndex - stepSize, indexStrategy.nextValue(index));
    }

    @Test
    void testIndexAfterGlskLimBelowReferenceThenUnsecureBelowReference() {
        double startingIndex = 2000;
        double stepSize = 650;
        Index<RaoResult> index = new Index<>(0, 5000, 50);
        IndexStrategy indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex, stepSize, 3000);
        index.addDichotomyStepResult(startingIndex, stepResultGlskLim);
        assertEquals(2000 + 650, indexStrategy.nextValue(index));
        index.addDichotomyStepResult(2650, stepResultNOk);
        assertEquals(2650 - (650. / 2), indexStrategy.nextValue(index));
    }

    @Test
    void testIndexAfterGlskLimBelowReferenceThenUnsecureAboveReference() {
        double startingIndex = 2000;
        double stepSize = 650;
        Index<RaoResult> index = new Index<>(0, 5000, 50);
        IndexStrategy indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex, stepSize, 2500);
        index.addDichotomyStepResult(startingIndex, stepResultGlskLim);
        assertEquals(2000 + 650, indexStrategy.nextValue(index));
        index.addDichotomyStepResult(2650, stepResultNOk);
        assertEquals(2650 - (650. / 2), indexStrategy.nextValue(index));
    }

    @Test
    void testIndexAfterTwoGlskLimBelowReferenceThenGlskLimAboveReference() {
        double startingIndex = 2000;
        double stepSize = 650;
        Index<RaoResult> index = new Index<>(0, 5000, 50);
        IndexStrategy indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex, stepSize, 3000);
        index.addDichotomyStepResult(startingIndex, stepResultGlskLim);
        assertEquals(2000 + 650, indexStrategy.nextValue(index));
        index.addDichotomyStepResult(2650, stepResultGlskLim);
        assertEquals(2650 + 650, indexStrategy.nextValue(index));
        index.addDichotomyStepResult(3300, stepResultGlskLim);
        assertEquals(3300 - 325, indexStrategy.nextValue(index));
    }

    @Test
    void testIndexAfterGlskLimBelowReferenceThenSecure() {
        double startingIndex = 2000;
        double stepSize = 650;
        Index<RaoResult> index = new Index<>(0, 5000, 50);
        IndexStrategy indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex, stepSize, 3000);
        index.addDichotomyStepResult(startingIndex, stepResultGlskLim);
        assertEquals(2000 + 650, indexStrategy.nextValue(index));
        index.addDichotomyStepResult(2650, stepResultOk);
        assertEquals(2650 + 650, indexStrategy.nextValue(index));
    }

    @Test
    void testIndexAfterGlskLimAboveReference() {
        double startingIndex = 2000;
        double stepSize = 650;
        Index<RaoResult> index = new Index<>(0, 5000, 50);
        IndexStrategy indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex, stepSize, 1500);
        index.addDichotomyStepResult(startingIndex, stepResultGlskLim);
        assertEquals(2000 - (650. / 2), indexStrategy.nextValue(index));
    }

    @Test
    void testIndexAfterGlskLimBelowReferenceThenUnsecureThenGlskLimThenSecure() {
        double startingIndex = 2000;
        double stepSize = 650;
        Index<RaoResult> index = new Index<>(0, 5000, 50);
        IndexStrategy indexStrategy = new BiDirectionalStepsWithReferenceIndexStrategy(startingIndex, stepSize, 2500);
        index.addDichotomyStepResult(startingIndex, stepResultGlskLim);
        assertEquals(2000 + 650, indexStrategy.nextValue(index));
        index.addDichotomyStepResult(2650, stepResultNOk);
        assertEquals(2650 - (650. / 2), indexStrategy.nextValue(index));
        index.addDichotomyStepResult(2650 - (650. / 2), stepResultGlskLim);
        assertEquals((2650. + 2325) / 2, indexStrategy.nextValue(index));

        index.addDichotomyStepResult(2487.5, stepResultOk);
        assertEquals((2650. + 2487.5) / 2, indexStrategy.nextValue(index));
    }

}
