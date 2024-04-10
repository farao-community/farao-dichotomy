/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api;

import com.farao_community.farao.dichotomy.api.exceptions.DichotomyException;
import com.farao_community.farao.dichotomy.api.exceptions.RaoInterruptionException;
import com.farao_community.farao.dichotomy.api.exceptions.ValidationException;
import com.farao_community.farao.dichotomy.api.index.Index;
import com.farao_community.farao.dichotomy.api.index.IndexStrategy;
import com.farao_community.farao.dichotomy.api.index.RangeDivisionIndexStrategy;
import com.farao_community.farao.dichotomy.api.index.StepsIndexStrategy;
import com.farao_community.farao.dichotomy.api.results.DichotomyResult;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 * @author Vincent Bochet {@literal <vincent.bochet at rte-france.com>}
 */
class DichotomyEngineTest {
    private static final double EPSILON = 1e-3;

    private Network initialNetwork;

    @BeforeEach
    void setUp() {
        String networkFilename = "20210901_2230_test_network.uct";
        initialNetwork = Network.read(networkFilename, getClass().getResourceAsStream(networkFilename));
    }

    @Test
    void checkRangeDivisionIndexStrategyStartingWithMin() {
        double limit = -340;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        Index<Object> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(true);
        NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        DichotomyEngine<Object> engine = new DichotomyEngine<>(index, indexStrategy, Mockito.mock(NetworkShifter.class), networkValidator);
        engine.run(initialNetwork);

        assertEquals(6, index.testedSteps().size());

        assertTrue(index.highestValidStep().getRight().isValid());
        assertEquals(-375, index.highestValidStep().getLeft(), EPSILON);
        assertFalse(index.lowestInvalidStep().getRight().isValid());
        assertEquals(-250, index.lowestInvalidStep().getLeft(), EPSILON);

        assertEquals(-1000, index.testedSteps().get(0).getLeft(), EPSILON);
        assertTrue(index.testedSteps().get(0).getRight().isValid());
        assertEquals(1000, index.testedSteps().get(1).getLeft(), EPSILON);
        assertFalse(index.testedSteps().get(1).getRight().isValid());
        assertEquals(0, index.testedSteps().get(2).getLeft(), EPSILON);
        assertFalse(index.testedSteps().get(2).getRight().isValid());
        assertEquals(-500, index.testedSteps().get(3).getLeft(), EPSILON);
        assertTrue(index.testedSteps().get(3).getRight().isValid());
        assertEquals(-250, index.testedSteps().get(4).getLeft(), EPSILON);
        assertFalse(index.testedSteps().get(4).getRight().isValid());
        assertEquals(-375, index.testedSteps().get(5).getLeft(), EPSILON);
        assertTrue(index.testedSteps().get(5).getRight().isValid());
    }

    @Test
    void checkRangeDivisionIndexStrategyStartingWithMax() {
        double limit = -340;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        Index<Object> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(false);
        NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        DichotomyEngine<Object> engine = new DichotomyEngine<>(index, indexStrategy, Mockito.mock(NetworkShifter.class), networkValidator);
        engine.run(initialNetwork);

        assertEquals(6, index.testedSteps().size());

        assertTrue(index.highestValidStep().getRight().isValid());
        assertEquals(-375, index.highestValidStep().getLeft(), EPSILON);
        assertFalse(index.lowestInvalidStep().getRight().isValid());
        assertEquals(-250, index.lowestInvalidStep().getLeft(), EPSILON);

        assertEquals(1000, index.testedSteps().get(0).getLeft(), EPSILON);
        assertFalse(index.testedSteps().get(0).getRight().isValid());
        assertEquals(-1000, index.testedSteps().get(1).getLeft(), EPSILON);
        assertTrue(index.testedSteps().get(1).getRight().isValid());
        assertEquals(0, index.testedSteps().get(2).getLeft(), EPSILON);
        assertFalse(index.testedSteps().get(2).getRight().isValid());
        assertEquals(-500, index.testedSteps().get(3).getLeft(), EPSILON);
        assertTrue(index.testedSteps().get(3).getRight().isValid());
        assertEquals(-250, index.testedSteps().get(4).getLeft(), EPSILON);
        assertFalse(index.testedSteps().get(4).getRight().isValid());
        assertEquals(-375, index.testedSteps().get(5).getLeft(), EPSILON);
        assertTrue(index.testedSteps().get(5).getRight().isValid());
    }

    @Test
    void checkStepsIndexStrategyStartingWithMin() {
        double limit = -340;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        double stepsSize = 400;
        Index<Object> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new StepsIndexStrategy(true, stepsSize);
        NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        DichotomyEngine<Object> engine = new DichotomyEngine<>(index, indexStrategy, Mockito.mock(NetworkShifter.class), networkValidator);
        engine.run(initialNetwork);

        assertEquals(4, index.testedSteps().size());

        assertTrue(index.highestValidStep().getRight().isValid());
        assertEquals(-400, index.highestValidStep().getLeft(), EPSILON);
        assertFalse(index.lowestInvalidStep().getRight().isValid());
        assertEquals(-200, index.lowestInvalidStep().getLeft(), EPSILON);

        assertEquals(-1000, index.testedSteps().get(0).getLeft(), EPSILON);
        assertTrue(index.testedSteps().get(0).getRight().isValid());
        assertEquals(-600, index.testedSteps().get(1).getLeft(), EPSILON);
        assertTrue(index.testedSteps().get(1).getRight().isValid());
        assertEquals(-200, index.testedSteps().get(2).getLeft(), EPSILON);
        assertFalse(index.testedSteps().get(2).getRight().isValid());
        assertEquals(-400, index.testedSteps().get(3).getLeft(), EPSILON);
        assertTrue(index.testedSteps().get(3).getRight().isValid());
    }

    @Test
    void checkStepsIndexStrategyStartingWithMax() {
        double limit = -340;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        double stepsSize = 400;
        Index<Object> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new StepsIndexStrategy(false, stepsSize);
        NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        DichotomyEngine<Object> engine = new DichotomyEngine<>(index, indexStrategy, Mockito.mock(NetworkShifter.class), networkValidator);
        engine.run(initialNetwork);

        assertEquals(6, index.testedSteps().size());

        assertTrue(index.highestValidStep().getRight().isValid());
        assertEquals(-400, index.highestValidStep().getLeft(), EPSILON);
        assertFalse(index.lowestInvalidStep().getRight().isValid());
        assertEquals(-200, index.lowestInvalidStep().getLeft(), EPSILON);

        assertEquals(1000, index.testedSteps().get(0).getLeft(), EPSILON);
        assertFalse(index.testedSteps().get(0).getRight().isValid());
        assertEquals(600, index.testedSteps().get(1).getLeft(), EPSILON);
        assertFalse(index.testedSteps().get(1).getRight().isValid());
        assertEquals(200, index.testedSteps().get(2).getLeft(), EPSILON);
        assertFalse(index.testedSteps().get(2).getRight().isValid());
        assertEquals(-200, index.testedSteps().get(3).getLeft(), EPSILON);
        assertFalse(index.testedSteps().get(3).getRight().isValid());
        assertEquals(-600, index.testedSteps().get(4).getLeft(), EPSILON);
        assertTrue(index.testedSteps().get(4).getRight().isValid());
        assertEquals(-400, index.testedSteps().get(5).getLeft(), EPSILON);
        assertTrue(index.testedSteps().get(5).getRight().isValid());
    }

    @Test
    void checkDichotomyEngineStopsAtMaxIterations() {
        double limit = -340;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        int maxIterations = 5;
        Index<Object> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(true);
        NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        DichotomyEngine<Object> engine = new DichotomyEngine<>(index, indexStrategy, Mockito.mock(NetworkShifter.class), networkValidator, maxIterations);
        engine.run(initialNetwork);

        assertEquals(5, index.testedSteps().size());

        assertTrue(index.highestValidStep().getRight().isValid());
        assertEquals(-500, index.highestValidStep().getLeft(), EPSILON);
        assertFalse(index.lowestInvalidStep().getRight().isValid());
        assertEquals(-250, index.lowestInvalidStep().getLeft(), EPSILON);

        assertEquals(-1000, index.testedSteps().get(0).getLeft(), EPSILON);
        assertTrue(index.testedSteps().get(0).getRight().isValid());
        assertEquals(1000, index.testedSteps().get(1).getLeft(), EPSILON);
        assertFalse(index.testedSteps().get(1).getRight().isValid());
        assertEquals(0, index.testedSteps().get(2).getLeft(), EPSILON);
        assertFalse(index.testedSteps().get(2).getRight().isValid());
        assertEquals(-500, index.testedSteps().get(3).getLeft(), EPSILON);
        assertTrue(index.testedSteps().get(3).getRight().isValid());
        assertEquals(-250, index.testedSteps().get(4).getLeft(), EPSILON);
        assertFalse(index.testedSteps().get(4).getRight().isValid());
    }

    @Test
    void checkAllSecure() {
        double limit = 1500;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        Index<Object> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(true);
        NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        DichotomyEngine<Object> engine = new DichotomyEngine<>(index, indexStrategy, Mockito.mock(NetworkShifter.class), networkValidator);
        engine.run(initialNetwork);

        assertEquals(2, index.testedSteps().size());

        assertTrue(index.highestValidStep().getRight().isValid());
        assertEquals(1000, index.highestValidStep().getLeft(), EPSILON);
        assertNull(index.lowestInvalidStep());

        assertEquals(-1000, index.testedSteps().get(0).getLeft(), EPSILON);
        assertTrue(index.testedSteps().get(0).getRight().isValid());
        assertEquals(1000, index.testedSteps().get(1).getLeft(), EPSILON);
        assertTrue(index.testedSteps().get(1).getRight().isValid());
    }

    @Test
    void checkAllUnsecure() {
        double limit = -1500;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        Index<Object> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(true);
        NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        DichotomyEngine<Object> engine = new DichotomyEngine<>(index, indexStrategy, Mockito.mock(NetworkShifter.class), networkValidator);
        engine.run(initialNetwork);

        assertEquals(1, index.testedSteps().size());

        assertNull(index.highestValidStep());
        assertFalse(index.lowestInvalidStep().getRight().isValid());
        assertEquals(-1000, index.lowestInvalidStep().getLeft(), EPSILON);

        assertEquals(-1000, index.testedSteps().get(0).getLeft(), EPSILON);
        assertFalse(index.testedSteps().get(0).getRight().isValid());
    }

    @Test
    void checkThatEngineFailsWhenMaxIterationTooLow() {
        double limit = -1500;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        Index<Object> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(true);
        NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        assertThrows(DichotomyException.class, () -> new DichotomyEngine<>(index, indexStrategy, Mockito.mock(NetworkShifter.class), networkValidator, 2));
    }

    @Test
    void checkThatStepsIndexStrategyCreationFailsWhenStepsSizeNegative() {
        assertThrows(DichotomyException.class, () -> new StepsIndexStrategy(true, -10));
        assertThrows(DichotomyException.class, () -> new StepsIndexStrategy(true, 0));
    }

    @Test
    void checkDichotomyEngineChecksLimitsWhenIntervalSmallerThanPrecision() {
        double limit = 0;
        double minValue = -50;
        double maxValue = 50;
        double precision = 200;
        int maxIterations = 5;
        Index<Object> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(true);
        NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        DichotomyEngine<Object> engine = new DichotomyEngine<>(index, indexStrategy, Mockito.mock(NetworkShifter.class), networkValidator, maxIterations);
        engine.run(initialNetwork);

        assertEquals(2, index.testedSteps().size());

        assertTrue(index.highestValidStep().getRight().isValid());
        assertEquals(-50, index.highestValidStep().getLeft(), EPSILON);
        assertFalse(index.lowestInvalidStep().getRight().isValid());
        assertEquals(50, index.lowestInvalidStep().getLeft(), EPSILON);

        assertEquals(-50, index.testedSteps().get(0).getLeft(), EPSILON);
        assertTrue(index.testedSteps().get(0).getRight().isValid());
        assertEquals(50, index.testedSteps().get(1).getLeft(), EPSILON);
        assertFalse(index.testedSteps().get(1).getRight().isValid());
    }

    @Test
    void checkDichotomyEngineStopEarlyWhenIntervalSmallerThanPrecisionButMinUnsecure() {
        double limit = -100;
        double minValue = -50;
        double maxValue = 50;
        double precision = 100;
        int maxIterations = 5;
        Index<Object> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(true);
        NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        DichotomyEngine<Object> engine = new DichotomyEngine<>(index, indexStrategy, Mockito.mock(NetworkShifter.class), networkValidator, maxIterations);
        engine.run(initialNetwork);

        assertEquals(1, index.testedSteps().size());

        assertNull(index.highestValidStep());
        assertFalse(index.lowestInvalidStep().getRight().isValid());
        assertEquals(-50, index.lowestInvalidStep().getLeft(), EPSILON);

        assertEquals(-50, index.testedSteps().get(0).getLeft(), EPSILON);
        assertFalse(index.testedSteps().get(0).getRight().isValid());
    }

    @Test
    void checkDichotomyEngineStopEarlyWhenIntervalSmallerThanPrecisionButMaxSecure() {
        double limit = 100;
        double minValue = -50;
        double maxValue = 50;
        double precision = 100;
        int maxIterations = 5;
        Index<Object> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(false);
        NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        DichotomyEngine<Object> engine = new DichotomyEngine<>(index, indexStrategy, Mockito.mock(NetworkShifter.class), networkValidator, maxIterations);
        engine.run(initialNetwork);

        assertEquals(1, index.testedSteps().size());

        assertTrue(index.highestValidStep().getRight().isValid());
        assertNull(index.lowestInvalidStep());
        assertEquals(50, index.highestValidStep().getLeft(), EPSILON);

        assertEquals(50, index.testedSteps().get(0).getLeft(), EPSILON);
        assertTrue(index.testedSteps().get(0).getRight().isValid());
    }

    @Test
    void checkSoftInterruptionNotInterrupted() {
        double limit = -340;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        double stepsSize = 400;
        Index<Object> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new StepsIndexStrategy(true, stepsSize);
        NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        InterruptionStrategy interruptionStrategy = Mockito.mock(InterruptionStrategy.class);
        Mockito.when(interruptionStrategy.shouldTaskBeInterruptedSoftly("id")).thenReturn(false);
        DichotomyEngine<Object> engine = new DichotomyEngine<>(index, indexStrategy, interruptionStrategy, Mockito.mock(NetworkShifter.class), networkValidator, "id");
        DichotomyResult<Object> dichotomyResult = engine.run(initialNetwork);

        assertFalse(dichotomyResult.isInterrupted());
        assertEquals(4, index.testedSteps().size());

        assertTrue(index.highestValidStep().getRight().isValid());
        assertEquals(-400, index.highestValidStep().getLeft(), EPSILON);
        assertFalse(index.lowestInvalidStep().getRight().isValid());
        assertEquals(-200, index.lowestInvalidStep().getLeft(), EPSILON);

        assertEquals(-1000, index.testedSteps().get(0).getLeft(), EPSILON);
        assertTrue(index.testedSteps().get(0).getRight().isValid());
        assertEquals(-600, index.testedSteps().get(1).getLeft(), EPSILON);
        assertTrue(index.testedSteps().get(1).getRight().isValid());
        assertEquals(-200, index.testedSteps().get(2).getLeft(), EPSILON);
        assertFalse(index.testedSteps().get(2).getRight().isValid());
        assertEquals(-400, index.testedSteps().get(3).getLeft(), EPSILON);
        assertTrue(index.testedSteps().get(3).getRight().isValid());
    }

    @Test
    void checkSoftInterruptionBeforeFirstRao() {
        double limit = -340;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        double stepsSize = 400;
        Index<Object> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new StepsIndexStrategy(true, stepsSize);
        NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        InterruptionStrategy interruptionStrategy = Mockito.mock(InterruptionStrategy.class);
        Mockito.when(interruptionStrategy.shouldTaskBeInterruptedSoftly("id")).thenReturn(true);
        DichotomyEngine<Object> engine = new DichotomyEngine<>(index, indexStrategy, interruptionStrategy, Mockito.mock(NetworkShifter.class), networkValidator, "id");
        DichotomyResult<Object> dichotomyResult = engine.run(initialNetwork);

        assertTrue(dichotomyResult.isInterrupted());
        assertEquals(0, index.testedSteps().size());
    }

    @Test
    void checkSoftInterruptionBetweenFirstAndSecondRao() {
        double limit = -340;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        double stepsSize = 400;
        Index<Object> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new StepsIndexStrategy(true, stepsSize);
        NetworkValidator<Object> networkValidator = new NetworkValidatorMock(limit);
        InterruptionStrategy interruptionStrategy = Mockito.mock(InterruptionStrategy.class);
        Mockito.when(interruptionStrategy.shouldTaskBeInterruptedSoftly("id")).thenReturn(false).thenReturn(true);
        DichotomyEngine<Object> engine = new DichotomyEngine<>(index, indexStrategy, interruptionStrategy, Mockito.mock(NetworkShifter.class), networkValidator, "id");
        DichotomyResult<Object> dichotomyResult = engine.run(initialNetwork);

        assertTrue(dichotomyResult.isInterrupted());
        assertEquals(1, index.testedSteps().size());
    }

    @Test
    void checkSoftInterruptionDuringRao() throws ValidationException, RaoInterruptionException {
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        double stepsSize = 400;
        Index<Object> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new StepsIndexStrategy(false, stepsSize);
        NetworkValidator<Object> networkValidator = Mockito.mock(NetworkValidator.class);
        Mockito.when(networkValidator.validateNetwork(Mockito.any(), Mockito.any())).thenThrow(new RaoInterruptionException("test"));
        InterruptionStrategy interruptionStrategy = Mockito.mock(InterruptionStrategy.class);
        Mockito.when(interruptionStrategy.shouldTaskBeInterruptedSoftly("id")).thenReturn(false).thenReturn(true);
        DichotomyEngine<Object> engine = new DichotomyEngine<>(index, indexStrategy, interruptionStrategy, Mockito.mock(NetworkShifter.class), networkValidator, "id");
        DichotomyResult<Object> dichotomyResult = engine.run(initialNetwork);

        assertTrue(dichotomyResult.isInterrupted());
        assertEquals(0, index.testedSteps().size());
    }
}
