/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.dichotomy.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class DichotomyEngineTest {
    private static final double EPSILON = 1e-3;

    @Test
    void checkRangeDivisionIndexStrategyStartingWithMin() {
        double limit = -340;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        Index<DefaultStepResult> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(true);
        ValidationStrategy<DefaultStepResult> validationStrategy = new ExampleValidationStrategy(limit);
        DichotomyEngine<DefaultStepResult> engine = new DichotomyEngine<>(index, indexStrategy, validationStrategy);
        engine.run();

        assertEquals(6, index.testedSteps().size());

        assertTrue(index.higherValidStep().isValid());
        assertEquals(-375, index.higherValidStep().stepValue(), EPSILON);
        assertFalse(index.lowerInvalidStep().isValid());
        assertEquals(-250, index.lowerInvalidStep().stepValue(), EPSILON);

        assertEquals(-1000, index.testedSteps().get(0).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(0).isValid());
        assertEquals(1000, index.testedSteps().get(1).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(1).isValid());
        assertEquals(0, index.testedSteps().get(2).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(2).isValid());
        assertEquals(-500, index.testedSteps().get(3).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(3).isValid());
        assertEquals(-250, index.testedSteps().get(4).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(4).isValid());
        assertEquals(-375, index.testedSteps().get(5).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(5).isValid());
    }

    @Test
    void checkRangeDivisionIndexStrategyStartingWithMax() {
        double limit = -340;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        Index<DefaultStepResult> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(false);
        ValidationStrategy<DefaultStepResult> validationStrategy = new ExampleValidationStrategy(limit);
        DichotomyEngine<DefaultStepResult> engine = new DichotomyEngine<>(index, indexStrategy, validationStrategy);
        engine.run();

        assertEquals(6, index.testedSteps().size());

        assertTrue(index.higherValidStep().isValid());
        assertEquals(-375, index.higherValidStep().stepValue(), EPSILON);
        assertFalse(index.lowerInvalidStep().isValid());
        assertEquals(-250, index.lowerInvalidStep().stepValue(), EPSILON);

        assertEquals(1000, index.testedSteps().get(0).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(0).isValid());
        assertEquals(-1000, index.testedSteps().get(1).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(1).isValid());
        assertEquals(0, index.testedSteps().get(2).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(2).isValid());
        assertEquals(-500, index.testedSteps().get(3).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(3).isValid());
        assertEquals(-250, index.testedSteps().get(4).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(4).isValid());
        assertEquals(-375, index.testedSteps().get(5).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(5).isValid());
    }

    @Test
    void checkStepsIndexStrategyStartingWithMin() {
        double limit = -340;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        double stepsSize = 400;
        Index<DefaultStepResult> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new StepsIndexStrategy(true, stepsSize);
        ValidationStrategy<DefaultStepResult> validationStrategy = new ExampleValidationStrategy(limit);
        DichotomyEngine<DefaultStepResult> engine = new DichotomyEngine<>(index, indexStrategy, validationStrategy);
        engine.run();

        assertEquals(5, index.testedSteps().size());

        assertTrue(index.higherValidStep().isValid());
        assertEquals(-400, index.higherValidStep().stepValue(), EPSILON);
        assertFalse(index.lowerInvalidStep().isValid());
        assertEquals(-300, index.lowerInvalidStep().stepValue(), EPSILON);

        assertEquals(-1000, index.testedSteps().get(0).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(0).isValid());
        assertEquals(-600, index.testedSteps().get(1).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(1).isValid());
        assertEquals(-200, index.testedSteps().get(2).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(2).isValid());
        assertEquals(-400, index.testedSteps().get(3).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(3).isValid());
        assertEquals(-300, index.testedSteps().get(4).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(4).isValid());
    }

    @Test
    void checkStepsIndexStrategyStartingWithMax() {
        double limit = -340;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        double stepsSize = 400;
        Index<DefaultStepResult> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new StepsIndexStrategy(false, stepsSize);
        ValidationStrategy<DefaultStepResult> validationStrategy = new ExampleValidationStrategy(limit);
        DichotomyEngine<DefaultStepResult> engine = new DichotomyEngine<>(index, indexStrategy, validationStrategy);
        engine.run();

        assertEquals(7, index.testedSteps().size());

        assertTrue(index.higherValidStep().isValid());
        assertEquals(-400, index.higherValidStep().stepValue(), EPSILON);
        assertFalse(index.lowerInvalidStep().isValid());
        assertEquals(-300, index.lowerInvalidStep().stepValue(), EPSILON);

        assertEquals(1000, index.testedSteps().get(0).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(0).isValid());
        assertEquals(600, index.testedSteps().get(1).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(1).isValid());
        assertEquals(200, index.testedSteps().get(2).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(2).isValid());
        assertEquals(-200, index.testedSteps().get(3).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(3).isValid());
        assertEquals(-600, index.testedSteps().get(4).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(4).isValid());
        assertEquals(-400, index.testedSteps().get(5).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(5).isValid());
        assertEquals(-300, index.testedSteps().get(6).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(6).isValid());
    }

    @Test
    void checkDichotomyEngineStopsAtMaxIterations() {
        double limit = -340;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        int maxIterations = 5;
        Index<DefaultStepResult> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(true);
        ValidationStrategy<DefaultStepResult> validationStrategy = new ExampleValidationStrategy(limit);
        DichotomyEngine<DefaultStepResult> engine = new DichotomyEngine<>(index, indexStrategy, validationStrategy, maxIterations);
        engine.run();

        assertEquals(5, index.testedSteps().size());

        assertTrue(index.higherValidStep().isValid());
        assertEquals(-500, index.higherValidStep().stepValue(), EPSILON);
        assertFalse(index.lowerInvalidStep().isValid());
        assertEquals(-250, index.lowerInvalidStep().stepValue(), EPSILON);

        assertEquals(-1000, index.testedSteps().get(0).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(0).isValid());
        assertEquals(1000, index.testedSteps().get(1).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(1).isValid());
        assertEquals(0, index.testedSteps().get(2).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(2).isValid());
        assertEquals(-500, index.testedSteps().get(3).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(3).isValid());
        assertEquals(-250, index.testedSteps().get(4).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(4).isValid());
    }

    @Test
    void checkAllSecure() {
        double limit = 1500;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        Index<DefaultStepResult> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(true);
        ValidationStrategy<DefaultStepResult> validationStrategy = new ExampleValidationStrategy(limit);
        DichotomyEngine<DefaultStepResult> engine = new DichotomyEngine<>(index, indexStrategy, validationStrategy);
        engine.run();

        assertEquals(2, index.testedSteps().size());

        assertTrue(index.higherValidStep().isValid());
        assertEquals(1000, index.higherValidStep().stepValue(), EPSILON);
        assertNull(index.lowerInvalidStep());

        assertEquals(-1000, index.testedSteps().get(0).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(0).isValid());
        assertEquals(1000, index.testedSteps().get(1).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(1).isValid());
    }

    @Test
    void checkAllUnsecure() {
        double limit = -1500;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        Index<DefaultStepResult> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(true);
        ValidationStrategy<DefaultStepResult> validationStrategy = new ExampleValidationStrategy(limit);
        DichotomyEngine<DefaultStepResult> engine = new DichotomyEngine<>(index, indexStrategy, validationStrategy);
        engine.run();

        assertEquals(1, index.testedSteps().size());

        assertNull(index.higherValidStep());
        assertFalse(index.lowerInvalidStep().isValid());
        assertEquals(-1000, index.lowerInvalidStep().stepValue(), EPSILON);

        assertEquals(-1000, index.testedSteps().get(0).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(0).isValid());
    }

    @Test
    void checkThatEngineFailsWhenMaxIterationTooLow() {
        double limit = -1500;
        double minValue = -1000;
        double maxValue = 1000;
        double precision = 200;
        Index<DefaultStepResult> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(true);
        ValidationStrategy<DefaultStepResult> validationStrategy = new ExampleValidationStrategy(limit);
        Assertions.assertThrows(DichotomyException.class, () -> new DichotomyEngine<>(index, indexStrategy, validationStrategy, 2));
    }

    @Test
    void checkThatStepsIndexStrategyCreationFailsWhenStepsSizeNegative() {
        Assertions.assertThrows(DichotomyException.class, () -> new StepsIndexStrategy(true, -10));
        Assertions.assertThrows(DichotomyException.class, () -> new StepsIndexStrategy(true, 0));
    }

    @Test
    void checkDichotomyEngineChecksLimitsWhenIntervalSmallerThanPrecision() {
        double limit = 0;
        double minValue = -50;
        double maxValue = 50;
        double precision = 200;
        int maxIterations = 5;
        Index<DefaultStepResult> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(true);
        ValidationStrategy<DefaultStepResult> validationStrategy = new ExampleValidationStrategy(limit);
        DichotomyEngine<DefaultStepResult> engine = new DichotomyEngine<>(index, indexStrategy, validationStrategy, maxIterations);
        engine.run();

        assertEquals(2, index.testedSteps().size());

        assertTrue(index.higherValidStep().isValid());
        assertEquals(-50, index.higherValidStep().stepValue(), EPSILON);
        assertFalse(index.lowerInvalidStep().isValid());
        assertEquals(50, index.lowerInvalidStep().stepValue(), EPSILON);

        assertEquals(-50, index.testedSteps().get(0).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(0).isValid());
        assertEquals(50, index.testedSteps().get(1).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(1).isValid());
    }

    @Test
    void checkDichotomyEngineStopEarlyWhenIntervalSmallerThanPrecisionButMinUnsecure() {
        double limit = -100;
        double minValue = -50;
        double maxValue = 50;
        double precision = 100;
        int maxIterations = 5;
        Index<DefaultStepResult> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(true);
        ValidationStrategy<DefaultStepResult> validationStrategy = new ExampleValidationStrategy(limit);
        DichotomyEngine<DefaultStepResult> engine = new DichotomyEngine<>(index, indexStrategy, validationStrategy, maxIterations);
        engine.run();

        assertEquals(1, index.testedSteps().size());

        assertNull(index.higherValidStep());
        assertFalse(index.lowerInvalidStep().isValid());
        assertEquals(-50, index.lowerInvalidStep().stepValue(), EPSILON);

        assertEquals(-50, index.testedSteps().get(0).stepValue(), EPSILON);
        assertFalse(index.testedSteps().get(0).isValid());
    }

    @Test
    void checkDichotomyEngineStopEarlyWhenIntervalSmallerThanPrecisionButMaxSecure() {
        double limit = 100;
        double minValue = -50;
        double maxValue = 50;
        double precision = 100;
        int maxIterations = 5;
        Index<DefaultStepResult> index = new Index<>(minValue, maxValue, precision);
        IndexStrategy indexStrategy = new RangeDivisionIndexStrategy(false);
        ValidationStrategy<DefaultStepResult> validationStrategy = new ExampleValidationStrategy(limit);
        DichotomyEngine<DefaultStepResult> engine = new DichotomyEngine<>(index, indexStrategy, validationStrategy, maxIterations);
        engine.run();

        assertEquals(1, index.testedSteps().size());

        assertTrue(index.higherValidStep().isValid());
        assertNull(index.lowerInvalidStep());
        assertEquals(50, index.higherValidStep().stepValue(), EPSILON);

        assertEquals(50, index.testedSteps().get(0).stepValue(), EPSILON);
        assertTrue(index.testedSteps().get(0).isValid());
    }
}
