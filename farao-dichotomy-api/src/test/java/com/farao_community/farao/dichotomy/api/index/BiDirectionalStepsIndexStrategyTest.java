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

        Index<?> index = new Index<>(0, 5000, 50);
        IndexStrategy indexStrategy = new BiDirectionalStepsIndexStrategy(startingIndex, 650);

        assertEquals(startingIndex, indexStrategy.nextValue(index));
    }

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
    void testIndexAfterFirstFailed() {
        double startingIndex = 2000;
        double stepSize = 650;

        Index<RaoResult> index = new Index<>(0, 5000, 50);
        IndexStrategy indexStrategy = new BiDirectionalStepsIndexStrategy(startingIndex, stepSize);

        index.addDichotomyStepResult(startingIndex, stepResultFailed);

        assertEquals(startingIndex - stepSize, indexStrategy.nextValue(index));
    }

    @Test
    void testIndexAfterInvalidCloseToMinimum() {
        double startingIndex = 2000;
        double stepSize = 650;
        double minValue = 0;

        Index<RaoResult> index = new Index<>(minValue, 5000, 50);
        IndexStrategy indexStrategy = new BiDirectionalStepsIndexStrategy(startingIndex, stepSize);

        index.addDichotomyStepResult(minValue + 100, stepResultNOk);

        assertEquals(minValue, indexStrategy.nextValue(index));
    }

    @Test
    void testIndexAfterFailedCloseToMinimum() {
        double startingIndex = 2000;
        double stepSize = 650;
        double minValue = 0;

        Index<RaoResult> index = new Index<>(minValue, 5000, 50);
        IndexStrategy indexStrategy = new BiDirectionalStepsIndexStrategy(startingIndex, stepSize);

        index.addDichotomyStepResult(minValue + 100, stepResultFailed);

        assertEquals(minValue, indexStrategy.nextValue(index));
    }

    @Test
    void testIndexAfterFirstValid() {
        double startingIndex = 2000;
        double stepSize = 650;

        Index<RaoResult> index = new Index<>(0, 5000, 50);
        IndexStrategy indexStrategy = new BiDirectionalStepsIndexStrategy(startingIndex, stepSize);

        index.addDichotomyStepResult(startingIndex, stepResultOk);

        assertEquals(startingIndex + stepSize, indexStrategy.nextValue(index));
    }

    @Test
    void testIndexAfterValidCloseToMaximum() {
        double startingIndex = 2000;
        double stepSize = 650;
        double maxValue = 5000;

        Index<RaoResult> index = new Index<>(0, maxValue, 50);
        IndexStrategy indexStrategy = new BiDirectionalStepsIndexStrategy(startingIndex, stepSize);

        index.addDichotomyStepResult(maxValue - 100, stepResultOk);

        assertEquals(maxValue, indexStrategy.nextValue(index));
    }

    @Test
    void testIndexAfterValidAndInvalidSteps() {
        double startingIndex = 2000;
        double stepSize = 650;

        Index<RaoResult> index = new Index<>(0, 5000, 50);
        IndexStrategy indexStrategy = new BiDirectionalStepsIndexStrategy(startingIndex, stepSize);

        index.addDichotomyStepResult(2000, stepResultOk);
        index.addDichotomyStepResult(3000, stepResultNOk);

        assertEquals(2500, indexStrategy.nextValue(index));
    }

    @Test
    void testIndexAfterValidAndFailedSteps() {
        double startingIndex = 2000;
        double stepSize = 650;

        Index<RaoResult> index = new Index<>(0, 5000, 50);
        IndexStrategy indexStrategy = new BiDirectionalStepsIndexStrategy(startingIndex, stepSize);

        index.addDichotomyStepResult(2000, stepResultOk);
        index.addDichotomyStepResult(3000, stepResultFailed);

        assertEquals(2500, indexStrategy.nextValue(index));
    }

    @Test
    void testIndexAfterPrecisionReached() {
        double startingIndex = 2000;
        double stepSize = 650;

        Index<RaoResult> index = new Index<>(0, 5000, 50);
        IndexStrategy indexStrategy = new BiDirectionalStepsIndexStrategy(startingIndex, stepSize);

        index.addDichotomyStepResult(2000, stepResultOk);
        index.addDichotomyStepResult(2020, stepResultNOk);

        assertThrows(AssertionError.class, () -> indexStrategy.nextValue(index));
    }
}
